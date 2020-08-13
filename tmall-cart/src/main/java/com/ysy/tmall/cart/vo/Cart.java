package com.ysy.tmall.cart.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车
 *
 * @anthor silenceYin
 * @date 2020/7/26 - 2:08
 */
public class Cart {
    /**
     * 购物车商品列表
     */
    private List<CartItem> items;

    /**
     * 商品数量合计
     */
    private Integer countNum;

    /**
     * 商品类型合计
     */
    private Integer countType;

    /**
     * 商品价格合计
     */
    private BigDecimal totalAmount;


    /**
     * 商品减免价格
     */
    private BigDecimal reduce = new BigDecimal("0.00");;

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count=0;
        if(items!=null&&items.size()>0){
            for (CartItem item : items) {
                count+=item.getCount();
            }
        }
        return count;
    }

    public Integer getCountType() {

        int count=0;
        if(items!=null&&items.size()>0){
            for (CartItem item : items) {
                count+=1;
            }
        }
        return count;
    }


    public BigDecimal getTotalAmount() {
        BigDecimal amount=new BigDecimal("0");
        //计算购物项总价
        if(items!=null&&items.size()>0){
            for (CartItem item : items) {
                if(item.getCheck()){
                    BigDecimal totalPrice = item.getTotalPrice();
                    amount= amount.add(totalPrice);
                }
            }
        }
        //减去优惠总价
        BigDecimal subtract = amount.subtract(getReduce());

        return subtract;
    }


    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}

