package com.idamart.service.impl;

import com.idamart.dto.CartItemRequest;
import com.idamart.dto.CartResponse;
import com.idamart.entity.CartItem;
import com.idamart.entity.Product;
import com.idamart.exception.BadRequestException;
import com.idamart.exception.ResourceNotFoundException;
import com.idamart.mapper.EntityMapper;
import com.idamart.repository.CartItemRepository;
import com.idamart.repository.ProductRepository;
import com.idamart.repository.UserRepository;
import com.idamart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    static final int MAX_QUANTITY_PER_ITEM = 10;

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        return EntityMapper.toCartResponse(cartItemRepository.findByUserIdWithProduct(userId));
    }

    @Override
    @Transactional
    public CartResponse addItem(Long userId, CartItemRequest request) {
        Product product = productRepository.findById(request.productId())
                .filter(Product::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.productId()));

        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, product.getId())
                .orElseGet(() -> CartItem.builder()
                        .user(userRepository.getReferenceById(userId))   // no extra SELECT needed
                        .product(product)
                        .quantity(0)
                        .build());

        int newQuantity = item.getQuantity() + request.quantity();
        checkQuantity(product, newQuantity);
        item.setQuantity(newQuantity);
        cartItemRepository.save(item);

        return getCart(userId);
    }

    @Override
    @Transactional
    public CartResponse updateQuantity(Long userId, Long productId, int quantity) {
        CartItem item = findItemOrThrow(userId, productId);
        checkQuantity(item.getProduct(), quantity);
        item.setQuantity(quantity);
        return getCart(userId);
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long userId, Long productId) {
        cartItemRepository.delete(findItemOrThrow(userId, productId));
        return getCart(userId);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteAllByUserId(userId);
    }

    // ---------------- helpers ----------------

    private CartItem findItemOrThrow(Long userId, Long productId) {
        return cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("This product is not in your cart"));
    }

    private void checkQuantity(Product product, int quantity) {
        if (quantity > MAX_QUANTITY_PER_ITEM) {
            throw new BadRequestException("You can buy at most " + MAX_QUANTITY_PER_ITEM + " of one item");
        }
        if (quantity > product.getStock()) {
            throw new BadRequestException("Only " + product.getStock() + " left in stock for '"
                    + product.getName() + "'");
        }
    }
}
