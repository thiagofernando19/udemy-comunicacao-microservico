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

connectMongoDb();
createInitialData();
connectRabbitMq();

app.use(express.json());
app.use(checkToken);
app.use(orderRoutes);

app.get("/teste", async (_req, res) => {
  try {
    sendMessageToProductStockUpdateQueue(
      {
        productId: 1001,
        quantity: 3,
      },
      {
        productId: 1002,
        quantity: 2,
      }
    );
    return res.status(200).json({ status: 200 });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ error: true });
  }
});

app.get("/api/status", async (_req, res) => {
  return res.status(200).json({
    service: "Sales-api",
    status: "up",
    httpStatus: 200,
  });
});

app.get("/api/all", async (_req, res) => {
  return res.status(200).json({
    service: "Sales-api",
    status: "up",
    httpStatus: 200,
  });
});
app.listen(PORT, () => {
  console.info("Server started successfully at port " + PORT);
});
