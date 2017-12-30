package com.e3mall.cart.controller;

import com.e3mall.cart.service.CartService;
import com.e3mall.pojo.TbItem;
import com.e3mall.pojo.TbUser;
import com.e3mall.service.ItemService;
import com.e3mall.utils.CookieUtils;
import com.e3mall.utils.E3Result;
import com.e3mall.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by qimenggao on 2017/12/30.
 */
@Controller
public class CartController {

    @Value("${COOKIE_NAME}")
    private String COOKIE_NAME;

    @Value("${COOKIE_CART_EXPIRE}")
    private Integer COOKIE_CART_EXPIRE;

    @Autowired
    private CartService cartService;

    @Autowired
    private ItemService itemService;

    @RequestMapping("/cart/add/{itemId}")
    public String addCartItem(@PathVariable Long itemId, @RequestParam(defaultValue = "1") Integer num, HttpServletRequest request, HttpServletResponse response) {
        Object userInfo = request.getAttribute("user");
        if(userInfo != null){
            TbUser user= (TbUser) userInfo;
            cartService.addCart(user.getId(),itemId,num);
            return "cartSuccess";
        }
//        1、从cookie中查询商品列表。
        List<TbItem> cartList = getCartListFromCookie(request);
//        2、判断商品在商品列表中是否存在。
        boolean hasItem = false;
        for (TbItem item : cartList) {
//        3、如果存在，商品数量相加。
            if (item.getId() == itemId.longValue()) {
                item.setNum(item.getNum() + num);
                hasItem = true;
                break;
            }
        }
//        4、不存在，根据商品id查询商品信息。
        if (!hasItem) {
            // 4、不存在，根据商品id查询商品信息。
            TbItem tbItem = itemService.selectByPrimaryKey(itemId);
            //取一张图片
            String image = tbItem.getImage();
            if (StringUtils.isNoneBlank(image)) {
                String[] images = image.split(",");
                tbItem.setImage(images[0]);
            }
            //设置购买商品数量
            tbItem.setNum(num);
            // 5、把商品添加到购车列表。
            cartList.add(tbItem);
        }
        // 6、把购车商品列表写入cookie。
        CookieUtils.setCookie(request, response, COOKIE_NAME, JsonUtils.objectToJson(cartList), COOKIE_CART_EXPIRE, true);
        return "cartSuccess";
    }

    public List<TbItem> getCartListFromCookie(HttpServletRequest request) {
        String json = CookieUtils.getCookieValue(request, COOKIE_NAME, true);
        if (StringUtils.isBlank(json)) {
            return new ArrayList<TbItem>();
        }
        List<TbItem> itemList = JsonUtils.jsonToList(json, TbItem.class);
        return itemList;
    }


    @RequestMapping("/cart/cart")
    public String showCartList(HttpServletRequest request) {
        List<TbItem> cartList = getCartListFromCookie(request);
        request.setAttribute("cartList", cartList);
        return "cart";
    }


    @RequestMapping("/cart/update/num/{itemId}/{num}")
    @ResponseBody
    public E3Result updateCartNum(@PathVariable("itemId") Long itemId, @PathVariable("num") Integer num, HttpServletRequest request, HttpServletResponse response) {
//       取商品
        List<TbItem> cartList = getCartListFromCookie(request);
//        遍历商品列表信息
        for (TbItem item:cartList){
//        更新数量
            if (item.getId() == itemId.longValue()){
                item.setNum(num);
                break;
            }
        }
//        把购物车写会cookie
        CookieUtils.setCookie(request,response,COOKIE_NAME,JsonUtils.objectToJson(cartList),COOKIE_CART_EXPIRE,true);
        // 返回成功信息
        return E3Result.ok();
    }

    @RequestMapping("/cart/delete/{itemId}")
    public String deleteCartItem(@PathVariable Long itemId, HttpServletRequest request,
                                 HttpServletResponse response) {
        // 1、从url中取商品id
        // 2、从cookie中取购物车商品列表
        List<TbItem> cartList = getCartListFromCookie(request);
        // 3、遍历列表找到对应的商品
        for (TbItem tbItem : cartList) {
            if (tbItem.getId() == itemId.longValue()) {
                // 4、删除商品。
                cartList.remove(tbItem);
                break;
            }
        }
        // 5、把商品列表写入cookie。
        CookieUtils.setCookie(request, response, COOKIE_NAME, JsonUtils.objectToJson(cartList), COOKIE_CART_EXPIRE, true);
        // 6、返回逻辑视图：在逻辑视图中做redirect跳转。
        return "redirect:/cart/cart.html";
    }

}
