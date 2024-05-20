import request from 'supertest';
import { usePool } from '../src/config/database';
import { makeExpressApp } from '../src';
import { Repository } from '../src/data/repository';
import { Config } from '../src/config';
import jwt from 'jsonwebtoken';

const pool = usePool();
const app = makeExpressApp(pool, new Repository(pool));

function generateAdminToken() {
  return jwt.sign({ role: 'admin' }, Config.auth.jwtSecret, {
    expiresIn: '30d',
  });
}

beforeEach(async () => {
  await pool.query('BEGIN');
});

afterEach(async () => {
  await pool.query('ROLLBACK');
});
afterAll(async () => {
  await pool.end();
});

// study tests

test('should fetch studies', async () => {
  const res = await request(app).get('/v1/study');
  expect(res.statusCode).toBe(200);
  expect(res.body).toBeInstanceOf(Array);
});

test('should create a study', async () => {
  const token = generateAdminToken();
  const res = await request(app)
    .post('/v1/study')
    .set({ Authorization: 'Bearer ' + token })
    .send({ enrolmentKey: 'key', name: 'name' });
  expect(res.statusCode).toBe(200);
  expect(res.body).toMatchObject({ enrolmentKey: 'key', name: 'name' });
});

test('should fetch a study by id', async () => {
  const token = generateAdminToken();
  const study = await request(app)
    .post('/v1/study')
    .set({ Authorization: 'Bearer ' + token })
    .send({ enrolmentKey: 'key', name: 'name' });

  const res = await request(app).get(`/v1/study/${study.body.id}`);
  expect(res.statusCode).toBe(200);
  expect(res.body).toMatchObject({
    enrolmentKey: 'key',
    name: 'name',
    id: study.body.id,
  });
});

test('should fail creating a study without required fields', async () => {
  const token = generateAdminToken();
  const res = await request(app)
    .post('/v1/study')
    .set({ Authorization: 'Bearer ' + token })
    .send({});
  expect(res.statusCode).toBe(400);
  expect(res.body).toEqual({
    error: 'Missing required fields (enrolmentKey or name)',
  });
});

test('should fail creating a study with duplicate enrolment key', async () => {
  const token = generateAdminToken();
  await request(app)
    .post('/v1/study')
    .set({ Authorization: 'Bearer ' + token })
    .send({ enrolmentKey: 'key', name: 'name' });

  const res = await request(app)
    .post('/v1/study')
    .set({ Authorization: 'Bearer ' + token })
    .send({ enrolmentKey: 'key', name: 'name' });

  expect(res.statusCode).toBe(400);
  expect(res.body).toEqual({
    error: 'Study with enrolment key already exists',
  });
});

// enrolment tests

test('should enrol in study', async () => {
  const token = generateAdminToken();
  const study = await request(app)
    .post('/v1/study')
    .set({ Authorization: 'Bearer ' + token })
    .send({ enrolmentKey: 'key', name: 'name' });

  const res = await request(app)
    .post('/v1/enrolment')
    .send({ enrolmentKey: 'key' });

  expect(res.statusCode).toBe(200);
  expect(res.body).toHaveProperty('participantId');
  expect(res.body).toHaveProperty('token');
});

test('should enrol in study with participant id', async () => {
  const token = generateAdminToken();
  const study = await request(app)
    .post('/v1/study')
    .set({ Authorization: 'Bearer ' + token })
    .send({ enrolmentKey: 'key', name: 'name' });

  const enrol = await request(app)
    .post('/v1/enrolment')
    .send({ enrolmentKey: 'key' });

  const res = await request(app).post(
    `/v1/enrolment/${enrol.body.participantId}`,
  );

  expect(res.statusCode).toBe(200);
  expect(res.body).toHaveProperty('participantId');
  expect(res.body).toHaveProperty('token');
});

// sensor reading tests

test('should create a sensor reading', async () => {
  const token = generateAdminToken();
  const study = await request(app)
    .post('/v1/study')
    .set({ Authorization: 'Bearer ' + token })
    .send({ enrolmentKey: 'key', name: 'name' });

  const enrol = await request(app)
    .post('/v1/enrolment')
    .send({ enrolmentKey: 'key' });

  const res = await request(app)
    .post('/v1/reading')
    .set({ Authorization: 'Bearer ' + enrol.body.token })
    .send({ sensorType: 'type', data: 'data' });

  expect(res.statusCode).toBe(200);
  expect(res.body).toMatchObject({ sensorType: 'type', data: 'data' });
});

test('should create a batch of sensor readings', async () => {
  const token = generateAdminToken();
  const study = await request(app)
    .post('/v1/study')
    .set({ Authorization: 'Bearer ' + token })
    .send({ enrolmentKey: 'key', name: 'name' });

  const enrol = await request(app)
    .post('/v1/enrolment')
    .send({ enrolmentKey: 'key' });

  const res = await request(app)
    .post('/v1/reading/batch')
    .set({ Authorization: 'Bearer ' + enrol.body.token })
    .send([
      { sensorType: 'type', data: 'data' },
      { sensorType: 'type', data: 'data' },
    ]);

  expect(res.statusCode).toBe(200);
  expect(res.body).toBeInstanceOf(Array);
  expect(res.body).toHaveLength(2);
  expect(res.body[0]).toMatchObject({ sensorType: 'type', data: 'data' });
  expect(res.body[1]).toMatchObject({ sensorType: 'type', data: 'data' });
});