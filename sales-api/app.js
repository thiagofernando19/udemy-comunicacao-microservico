import express from "express";

import { connectMongoDb } from "./src/config/db/mongoDbConfig.js";
import { createInitialData } from "./src/config/db/initialData.js";
import { connectRabbitMq } from "./src/config/rabbitmq/rabbitConfig.js";

import { sendMessageToProductStockUpdateQueue } from "./src/modules/product/rabbitmq/productStockUpdateSender.js";
import orderRoutes from "./src/modules/sales/routes/OrderRoutes.js";
import checkToken from "./src/config/auth/checkToken.js";

const app = express();

const env = process.env;
const PORT = env.PORT || 8082;
const THREE_MINUTES = 180000;

startApplication();

function startApplication() {
  if (env.NODE_ENV !== CONTAINER_ENV) {
    console.info("Waiting for RabbitMQ and MongoDB containers to start...");
    createInitialData();
    setInterval(() => {
      connectMongoDb();
      connectRabbitMq();
    }, THREE_MINUTES);
  } else {
    createInitialData();
    connectMongoDb();
    connectRabbitMq();
  }
}

app.use(express.json());

app.get("/api/initial-data", (_req, res) => {
  createInitialData();
  return res.status(200).json({ message: "Data created." });
});

app.use(checkToken);
app.use(orderRoutes);

app.get("/api/status", async (_req, res) => {
  return res.status(200).json({
    service: "Sales-api",
    status: "up",
    httpStatus: 200,
  });
});

app.listen(PORT, () => {
  console.info("Server started successfully at port " + PORT);
});
