import express from "express";
import { createInitialData } from "./src/config/db/initialData.js";
import userRoutes from "./src/modules/user/routes/UserRoutes.js";

const app = express();

const env = process.env;
const PORT = env.PORT || 8080;

app.get("/api/status", (_req, res) => {
  return res.status(200).json({
    service: "Auth-API",
    status: "up",
    httpStatus: 200,
  });
});

app.use(express.json());

startApplication();

function startApplication() {
  if (env.NODE_ENV !== CONTAINER_ENV) {
    createInitialData();
  }
}

app.get("/api/initial-data", (_req, res) => {
  createInitialData();
  return res.status(200).json({ message: "Data created." });
});

app.use(userRoutes);

app.listen(PORT, () => {
  console.info("Server started successfully at port " + PORT);
});
