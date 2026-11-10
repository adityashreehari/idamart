package com.idamart;

import com.idamart.entity.CartItem;
import com.idamart.entity.Category;
import com.idamart.entity.Product;
import com.idamart.entity.Role;
import com.idamart.entity.User;

import java.math.BigDecimal;

/** Small factory methods so every test builds objects the same, readable way. */
public final class TestData {

    private TestData() {
    }

    public static Category category(long id, String name) {
        Category c = Category.builder().name(name).description(name + " description").build();
        c.setId(id);
        return c;
    }

    public static Product product(long id, String name, String price, int stock) {
        Product p = Product.builder()
                .name(name)
                .description("desc")
                .animeSeries("Naruto")
                .price(new BigDecimal(price))
                .stock(stock)
                .imageUrl("")
                .active(true)
                .category(category(1, "Figures"))
                .build();
        p.setId(id);
        return p;
    }

    public static User user(long id, Role role) {
        User u = User.builder()
                .fullName("Test User")
                .email("user" + id + "@test.com")
                .password("HASH")
                .role(role)
                .build();
        u.setId(id);
        return u;
    }

    public static CartItem cartItem(User user, Product product, int quantity) {
        CartItem item = CartItem.builder().user(user).product(product).quantity(quantity).build();
        item.setId(product.getId() * 100);
        return item;
    }
}
