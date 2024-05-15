export const Config = {
  app: {
    hostname: process.env.APP_HOSTNAME || 'localhost',
    port: process.env.APP_PORT || 3000,
    uploadLocation: process.env.APP_UPLOAD_LOCATION || './uploads',
  },
  database: {
    connectionString: process.env.DB_CONNECTION || 'localhost:5432',
  },
  auth: {
    jwtSecret:
      process.env.AUTH_JWT_SECRET || 'change this or suffer the consequences',
  },
};
