import jwt from "jsonwebtoken";
import { promisify } from "util";
import AuthException from "./AuthException";

import * as secrets from "../constants/secrets";
import * as httpStatus from "../constants/httpStatus";

const bearer = "bearer ";
const emptySpace = " ";

export default async (req, res, next) => {
  const { authorization } = req.headers;
  try {
    if (!authorization) {
      throw new AuthException(
        httpStatus.UNAUTHORIZED,
        "Access token was not informed."
      );
    }
    let accessToken = authorization;
    if (accessToken.includes(emptySpace)) {
      accessToken = accessToken.split(emptySpace)[1];
    } else {
      accessToken = authorization;
    }
    const decoded = await promisify(jwt.verify)(
      accessToken,
      secrets.API_SECRET
    );
    req.authUser = decoded.authUser;
    return next();
  } catch (err) {
    const status = err.status ? err.status : httpStatus.INTERNAL_SERVER_ERROR;
    console.log(err);
    return res.status(status).json({
      status,
      message: err.message,
    });
  }
};
