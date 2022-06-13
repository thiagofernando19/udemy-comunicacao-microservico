import Sequelize from "sequelize";
import {
  DB_NAME,
  DB_HOST,
  DB_PORT,
  DB_USER,
  DB_PASSWORD,
} from "../constants/secrets";
const sequelize = new Sequelize(DB_NAME, DB_USER, DB_PASSWORD, {
  host: DB_HOST,
  port: DB_PORT,
  dialect: "postgres",
  quoteIdentifiers: false,
  define: {
    syncOnAssociation: true,
    timestamps: false,
    underscored: true,
    underscoredAll: true,
    freezeTableName: true,
  },
});

sequelize.authenticate().then(() => {
  console.info("Connection has been established!");
});
/*.cath((err) => {
    console.error("Unable to connect to the database");
    console.error(err.message);
  });*/

export default sequelize;
