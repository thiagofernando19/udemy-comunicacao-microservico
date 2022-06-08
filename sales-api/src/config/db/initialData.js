import Order from "../../modules/sales/model/Order.js";

export async function createInitialData() {
  let initialData = await Order.find();
  if (initialData) {
    await Order.collection.drop();
  }

  await Order.create({
    products: [
      {
        productId: 1001,
        quantity: 2,
      },
      {
        productId: 1002,
        quantity: 1,
      },
      {
        productId: 1003,
        quantity: 1,
      },
    ],
    user: {
      id: "skaopskpakps",
      name: "User Test",
      email: "userteste@example.com",
    },
    status: "APPROVED",
    createdAt: new Date(),
    updateAt: new Date(),
  });

  await Order.create({
    products: [
      {
        productId: 1001,
        quantity: 4,
      },
      {
        productId: 1002,
        quantity: 2,
      },
    ],
    user: {
      id: "askpoakops",
      name: "User Test 2",
      email: "userteste 2@example.com",
    },
    status: "REJECTED",
    createdAt: new Date(),
    updateAt: new Date(),
  });

  initialData = await Order.find();
  console.info(
    `Initial data was created: ${JSON.stringify(initialData, undefined, 4)}`
  );
}
