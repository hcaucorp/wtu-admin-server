import { Sequelize } from 'sequelize-typescript';
import { dbconfig } from './config';
export const sequelize = new Sequelize({
    database: dbconfig.database,
    dialect: dbconfig.dialect,
    username: dbconfig.username,
    password: dbconfig.password,
    host: dbconfig.host,
    port: dbconfig.port,
});
