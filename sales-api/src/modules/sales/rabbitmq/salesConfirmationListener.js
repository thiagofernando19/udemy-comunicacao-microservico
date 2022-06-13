import amqp from "amqplib/callback_api.js";

import { RABBIT_MQ_URL } from "../../../config/constants/secrets.js";
import { SALES_CONFIRMATION_QUEUE } from "../../../config/rabbitmq/queue.js";
import OrderService from "../service/OrderService.js";
export function listenToSalesConfirmationQueue() {
  console.log("Call listening");
  amqp.connect(RABBIT_MQ_URL, (error, connection) => {
    if (error) {
      throw error;
    }
    console.info("Listening to sales confirmation Queue...");
    connection.createChannel((err, channel) => {
      if (err) {
        throw err;
      }
      channel.consume(
        SALES_CONFIRMATION_QUEUE,
        (message) => {
          console.info(
            `Receiving message from queue: ${message.content.toString()}`
          );
          OrderService.updateOrder(message.content.toString());
        },
        {
          noAck: true,
        }
      );
    });
  });
}
