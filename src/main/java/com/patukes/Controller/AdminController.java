package com.patukes.Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.validation.Valid;

import com.patukes.Response.ServerResp;
import com.patukes.Response.order;
import com.patukes.Response.prodResp;
import com.patukes.Response.viewOrdResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.patukes.constants.ResponseCode;
import com.patukes.constants.WebConstants;
import com.patukes.model.PlaceOrder;
import com.patukes.model.Product;
import com.patukes.model.User;
import com.patukes.Repository.CartRepository;
import com.patukes.Repository.OrderRepository;
import com.patukes.Repository.ProductRepository;
import com.patukes.Repository.UserRepository;

import com.patukes.util.Validator;
import com.patukes.util.jwtUtil;

@CrossOrigin(origins = WebConstants.ALLOWED_URL)
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ProductRepository prodRepo;

    @Autowired
    private OrderRepository ordRepo;

    @Autowired
    private CartRepository cartRepo;

    @Autowired
    private jwtUtil jwtutil;

    @PostMapping("/verify")
    public ResponseEntity<ServerResp> verifyUser(@Valid @RequestBody HashMap<String, String> credential) {
        String email = "";
        String password = "";
        if (credential.containsKey(WebConstants.USER_EMAIL)) {
            email = credential.get(WebConstants.USER_EMAIL);
        }
        if (credential.containsKey(WebConstants.USER_PASSWORD)) {
            password = credential.get(WebConstants.USER_PASSWORD);
        }
        User loggedUser = userRepo.findByEmailAndPasswordAndUsertype(email, password, WebConstants.USER_ADMIN_ROLE);
        ServerResp resp = new ServerResp();
        if (loggedUser != null) {
            String jwtToken = jwtutil.createToken(email, password, WebConstants.USER_ADMIN_ROLE);
            resp.setStatus(ResponseCode.SUCCESS_CODE);
            resp.setMessage(ResponseCode.SUCCESS_MESSAGE);
            resp.setAUTH_TOKEN(jwtToken);
        } else {
            resp.setStatus(ResponseCode.FAILURE_CODE);
            resp.setMessage(ResponseCode.FAILURE_MESSAGE);
        }
        return new ResponseEntity<ServerResp>(resp, HttpStatus.OK);
    }

    @PostMapping("/addProduct")
    public ResponseEntity<prodResp> addProduct(@RequestHeader(name = WebConstants.USER_AUTH_TOKEN) String AUTH_TOKEN,
                                               @RequestParam(name = WebConstants.PROD_FILE) MultipartFile prodImage,
                                               @RequestParam(name = WebConstants.PROD_DESC) String description,
                                               @RequestParam(name = WebConstants.PROD_PRICE) String price,
                                               @RequestParam(name = WebConstants.PROD_NAME) String productname,
                                               @RequestParam(name = WebConstants.PROD_QUANITY) String quantity) throws IOException {
        prodResp resp = new prodResp();
        if (Validator.isStringEmpty(productname) || Validator.isStringEmpty(description)
                || Validator.isStringEmpty(price) || Validator.isStringEmpty(quantity)) {
            resp.setStatus(ResponseCode.BAD_REQUEST_CODE);
            resp.setMessage(ResponseCode.BAD_REQUEST_MESSAGE);
        } else if (!Validator.isStringEmpty(AUTH_TOKEN) && jwtutil.checkToken(AUTH_TOKEN) != null) {
            try {
                Product prod = new Product();
                prod.setDescription(description);
                prod.setPrice(Double.parseDouble(price));
                prod.setProductname(productname);
                prod.setQuantity(Integer.parseInt(quantity));
                prod.setProductimage(prodImage.getBytes());
                prodRepo.save(prod);

                resp.setStatus(ResponseCode.SUCCESS_CODE);
                resp.setMessage(ResponseCode.ADD_SUCCESS_MESSAGE);
                resp.setAUTH_TOKEN(AUTH_TOKEN);
                resp.setOblist(prodRepo.findAll());
            } catch (Exception e) {
                resp.setStatus(ResponseCode.FAILURE_CODE);
                resp.setMessage(e.getMessage());
                resp.setAUTH_TOKEN(AUTH_TOKEN);
            }
        } else {
            resp.setStatus(ResponseCode.BAD_REQUEST_CODE);
            resp.setMessage(ResponseCode.BAD_REQUEST_MESSAGE);
        }
        return new ResponseEntity<prodResp>(resp, HttpStatus.ACCEPTED);
    }

    @PostMapping("/getProducts")
    public ResponseEntity<prodResp> getProducts(@RequestHeader(name = WebConstants.USER_AUTH_TOKEN) String AUTH_TOKEN)
            throws IOException {
        prodResp resp = new prodResp();
        if (!Validator.isStringEmpty(AUTH_TOKEN) && jwtutil.checkToken(AUTH_TOKEN) != null) {
            try {
                resp.setStatus(ResponseCode.SUCCESS_CODE);
                resp.setMessage(ResponseCode.LIST_SUCCESS_MESSAGE);
                resp.setAUTH_TOKEN(AUTH_TOKEN);
                resp.setOblist(prodRepo.findAll());
            } catch (Exception e) {
                resp.setStatus(ResponseCode.FAILURE_CODE);
                resp.setMessage(e.getMessage());
                resp.setAUTH_TOKEN(AUTH_TOKEN);
            }
        } else {
            resp.setStatus(ResponseCode.FAILURE_CODE);
            resp.setMessage(ResponseCode.FAILURE_MESSAGE);
        }
        return new ResponseEntity<prodResp>(resp, HttpStatus.ACCEPTED);
    }

    @PostMapping("/updateProducts")
    public ResponseEntity<ServerResp> updateProducts(
            @RequestHeader(name = WebConstants.USER_AUTH_TOKEN) String AUTH_TOKEN,
            @RequestParam(name = WebConstants.PROD_FILE, required = false) MultipartFile prodImage,
            @RequestParam(name = WebConstants.PROD_DESC) String description,
            @RequestParam(name = WebConstants.PROD_PRICE) String price,
            @RequestParam(name = WebConstants.PROD_NAME) String productname,
            @RequestParam(name = WebConstants.PROD_QUANITY) String quantity,
            @RequestParam(name = WebConstants.PROD_ID) String productid) throws IOException {
        ServerResp resp = new ServerResp();
        if (Validator.isStringEmpty(productname) || Validator.isStringEmpty(description)
                || Validator.isStringEmpty(price) || Validator.isStringEmpty(quantity)) {
            resp.setStatus(ResponseCode.BAD_REQUEST_CODE);
            resp.setMessage(ResponseCode.BAD_REQUEST_MESSAGE);
        } else if (!Validator.isStringEmpty(AUTH_TOKEN) && jwtutil.checkToken(AUTH_TOKEN) != null) {
            try {
                Product prodOrg;
                Product prod;
                if (prodImage != null) {
                    prod = new Product(Integer.parseInt(productid), description, productname, Double.parseDouble(price),
                            Integer.parseInt(quantity), prodImage.getBytes());
                } else {
                    prodOrg = prodRepo.findByProductid(Integer.parseInt(productid));
                    prod = new Product(Integer.parseInt(productid), description, productname, Double.parseDouble(price),
                            Integer.parseInt(quantity), prodOrg.getProductimage());
                }
                prodRepo.save(prod);
                resp.setStatus(ResponseCode.SUCCESS_CODE);
                resp.setMessage(ResponseCode.UPD_SUCCESS_MESSAGE);
                resp.setAUTH_TOKEN(AUTH_TOKEN);
            } catch (Exception e) {
                resp.setStatus(ResponseCode.FAILURE_CODE);
                resp.setMessage(e.getMessage());
                resp.setAUTH_TOKEN(AUTH_TOKEN);
            }
        } else {
            resp.setStatus(ResponseCode.FAILURE_CODE);
            resp.setMessage(ResponseCode.FAILURE_MESSAGE);
        }
        return new ResponseEntity<ServerResp>(resp, HttpStatus.ACCEPTED);
    }

    @GetMapping("/delProduct")
    public ResponseEntity<prodResp> delProduct(@RequestHeader(name = WebConstants.USER_AUTH_TOKEN) String AUTH_TOKEN,
                                               @RequestParam(name = WebConstants.PROD_ID) String productid) throws IOException {
        prodResp resp = new prodResp();
        if (Validator.isStringEmpty(productid)) {
            resp.setStatus(ResponseCode.BAD_REQUEST_CODE);
            resp.setMessage(ResponseCode.BAD_REQUEST_MESSAGE);
        } else if (!Validator.isStringEmpty(AUTH_TOKEN) && jwtutil.checkToken(AUTH_TOKEN) != null) {
            try {
                prodRepo.deleteByProductid(Integer.parseInt(productid));
                resp.setStatus(ResponseCode.SUCCESS_CODE);
                resp.setMessage(ResponseCode.DEL_SUCCESS_MESSAGE);
                resp.setAUTH_TOKEN(AUTH_TOKEN);
                resp.setOblist(prodRepo.findAll());
            } catch (Exception e) {
                resp.setStatus(ResponseCode.FAILURE_CODE);
                resp.setMessage(e.toString());
                resp.setAUTH_TOKEN(AUTH_TOKEN);
            }
        } else {
            resp.setStatus(ResponseCode.FAILURE_CODE);
            resp.setMessage(ResponseCode.FAILURE_MESSAGE);
        }
        return new ResponseEntity<prodResp>(resp, HttpStatus.ACCEPTED);
    }

    @GetMapping("/viewOrders")
    public ResponseEntity<viewOrdResp> viewOrders(@RequestHeader(name = WebConstants.USER_AUTH_TOKEN) String AUTH_TOKEN)
            throws IOException {

        viewOrdResp resp = new viewOrdResp();
        if (!Validator.isStringEmpty(AUTH_TOKEN) && jwtutil.checkToken(AUTH_TOKEN) != null) {
            try {
                resp.setStatus(ResponseCode.SUCCESS_CODE);
                resp.setMessage(ResponseCode.VIEW_SUCCESS_MESSAGE);
                resp.setAUTH_TOKEN(AUTH_TOKEN);
                List<order> orderList = new ArrayList<>();
                List<PlaceOrder> poList = ordRepo.findAll();
                poList.forEach((po) -> {
                    order ord = new order();
                    ord.setOrderBy(po.getEmail());
                    ord.setOrderId(po.getOrderId());
                    ord.setOrderStatus(po.getOrderStatus());
                    ord.setProducts(cartRepo.findAllByOrderId(po.getOrderId()));
                    orderList.add(ord);
                });
                resp.setOrderlist(orderList);
            } catch (Exception e) {
                resp.setStatus(ResponseCode.FAILURE_CODE);
                resp.setMessage(e.getMessage());
                resp.setAUTH_TOKEN(AUTH_TOKEN);
            }
        } else {
            resp.setStatus(ResponseCode.FAILURE_CODE);
            resp.setMessage(ResponseCode.FAILURE_MESSAGE);
        }
        return new ResponseEntity<viewOrdResp>(resp, HttpStatus.ACCEPTED);
    }

    @PostMapping("/updateOrder")
    public ResponseEntity<ServerResp> updateOrders(
            @RequestHeader(name = WebConstants.USER_AUTH_TOKEN) String AUTH_TOKEN,
            @RequestParam(name = WebConstants.ORD_ID) String orderId,
            @RequestParam(name = WebConstants.ORD_STATUS) String orderStatus) throws IOException {

        ServerResp resp = new ServerResp();
        if (Validator.isStringEmpty(orderId) || Validator.isStringEmpty(orderStatus)) {
            resp.setStatus(ResponseCode.BAD_REQUEST_CODE);
            resp.setMessage(ResponseCode.BAD_REQUEST_MESSAGE);
        } else if (!Validator.isStringEmpty(AUTH_TOKEN) && jwtutil.checkToken(AUTH_TOKEN) != null) {
            try {
                PlaceOrder pc = ordRepo.findByOrderId(Integer.parseInt(orderId));
                pc.setOrderStatus(orderStatus);
                ordRepo.save(pc);
                resp.setStatus(ResponseCode.SUCCESS_CODE);
                resp.setMessage(ResponseCode.UPD_ORD_SUCCESS_MESSAGE);
                resp.setAUTH_TOKEN(AUTH_TOKEN);
            } catch (Exception e) {
                resp.setStatus(ResponseCode.FAILURE_CODE);
                resp.setMessage(e.toString());
                resp.setAUTH_TOKEN(AUTH_TOKEN);
            }
        } else {
            resp.setStatus(ResponseCode.FAILURE_CODE);
            resp.setMessage(ResponseCode.FAILURE_MESSAGE);
        }
        return new ResponseEntity<ServerResp>(resp, HttpStatus.ACCEPTED);
    }
}