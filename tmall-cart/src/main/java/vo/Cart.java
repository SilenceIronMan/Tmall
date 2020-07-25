package vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车
 *
 * @anthor silenceYin
 * @date 2020/7/26 - 2:08
 */
@Data
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
    private BigDecimal reduce;
}

