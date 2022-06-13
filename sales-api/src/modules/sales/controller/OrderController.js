import OrderService from "../service/OrderService";

class OrderController {
  async createOrder(req, res) {
    let order = await OrderService.createOrder(req);
    return res.status(order.status).json(order);
  }

  async findById(req, res) {
    let order = await OrderService.findById(req);
    return res.status(order.status).json(order);
  }

  async findAll(_req, res) {
    let order = await OrderService.findAll();
    return res.status(order.status).json(order);
  }

  async findByProductId(req, res) {
    let order = await OrderService.findbyProductId(req);
    return res.status(order.status).json(order);
  }
}

export default new OrderController();
