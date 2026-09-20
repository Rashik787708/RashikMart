<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.rashik.rashikmart.model.User" %>
<%@ page import="com.rashik.rashikmart.model.Product" %>
<%@ page import="com.rashik.rashikmart.model.Review" %>
<%@ page import="com.rashik.rashikmart.dao.ProductDAO" %>
<%@ page import="com.rashik.rashikmart.dao.CartDAO" %>
<%@ page import="com.rashik.rashikmart.dao.ReviewDAO" %>
<%@ page import="java.util.List" %>
<%@ page import="com.rashik.rashikmart.util.HtmlUtil" %>
<%@ page import="com.rashik.rashikmart.util.CsrfUtil" %>

<%
    User user = (User) session.getAttribute("user");

    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Please+login+first");
        return;
    }

    String role = user.getRole();
    if (role == null || !"BUYER".equalsIgnoreCase(role)) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?error=Buyer+access+required");
        return;
    }

    CsrfUtil.getOrCreateToken(session);

    Product product = (Product) request.getAttribute("product");
    if (product == null) {
        String idText = request.getParameter("id");
        if (idText != null && !idText.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idText.trim());
                ProductDAO productDAO = new ProductDAO();
                product = productDAO.findById(id);
            } catch (Exception ignored) {}
        }
    }

    if (product == null) {
        response.sendRedirect(request.getContextPath() + "/buyer/marketplace?error=Product+not+found");
        return;
    }

    CartDAO cartDAO = new CartDAO();
    int cartCount = cartDAO.getCartItems(user.getId()).size();

    List<Review> reviews = (List<Review>) request.getAttribute("reviews");
    int reviewCount = 0;
    double avgRating = 0.0;
    boolean hasPurchased = false;
    boolean hasReviewed = false;

    if (reviews == null) {
        ReviewDAO reviewDAO = new ReviewDAO();
        reviews = reviewDAO.findByProductId(product.getId());
        reviewCount = reviewDAO.getReviewCount(product.getId());
        avgRating = reviewDAO.getAverageRating(product.getId());
        hasPurchased = reviewDAO.hasPurchasedProduct(user.getId(), product.getId());
        hasReviewed = reviewDAO.hasReviewByBuyer(user.getId(), product.getId());
    } else {
        Object countAttr = request.getAttribute("reviewCount");
        if (countAttr instanceof Integer) {
            reviewCount = (Integer) countAttr;
        }
        Object avgAttr = request.getAttribute("avgRating");
        if (avgAttr instanceof Double) {
            avgRating = (Double) avgAttr;
        }
        Object purchasedAttr = request.getAttribute("hasPurchased");
        if (purchasedAttr instanceof Boolean) {
            hasPurchased = (Boolean) purchasedAttr;
        }
        Object reviewedAttr = request.getAttribute("hasReviewed");
        if (reviewedAttr instanceof Boolean) {
            hasReviewed = (Boolean) reviewedAttr;
        }
    }

    if (reviews == null) {
        reviews = new java.util.ArrayList<>();
    }

    String success = request.getParameter("success");
    String error = request.getParameter("error");

    String pImg = "default-product.svg";
    try {
        if (product.getImageUrl() != null && !product.getImageUrl().trim().isEmpty()) {
            pImg = product.getImageUrl().trim();
        }
    } catch (Throwable ignored) {}
    String imgSrc = pImg.startsWith("default-") ? request.getContextPath() + "/images/" + pImg : request.getContextPath() + "/images/products/" + pImg;
    boolean inStock = product.getQuantity() > 0;
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= HtmlUtil.escape(product.getName()) %> - RashikMart</title>
    <link rel="icon" href="data:,">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=20260828_4">
</head>
<body>

    <!-- Navigation Bar -->
    <header class="navbar">
        <a href="${pageContext.request.contextPath}/buyer/marketplace" class="brand">RashikMart</a>
        <nav>
            <ul class="nav-links">
                <li><a href="${pageContext.request.contextPath}/buyer/marketplace" class="nav-link active">Marketplace</a></li>
                <li><a href="${pageContext.request.contextPath}/buyer/cart" class="nav-link">My Cart (<%= cartCount %>)</a></li>
                <li><a href="${pageContext.request.contextPath}/buyer/orders" class="nav-link">My Orders</a></li>
                <li><a href="${pageContext.request.contextPath}/logout" class="nav-link">Logout</a></li>
            </ul>
        </nav>
    </header>

    <!-- Main Content -->
    <main class="page">
        <div class="seller-container" style="max-width: 900px; width: 100%;">

            <% if (success != null && !success.trim().isEmpty()) {
                String safeSuccess = HtmlUtil.escape(success);
            %>
                <div class="message success"><%= safeSuccess %></div>
            <% } %>

            <% if (error != null && !error.trim().isEmpty()) {
                String safeError = HtmlUtil.escape(error);
            %>
                <div class="message error"><%= safeError %></div>
            <% } %>

            <div class="seller-status-card" style="padding: 2.5rem; display: flex; gap: 2.5rem; align-items: flex-start; flex-wrap: wrap;">

                <!-- Product Image Section -->
                <div style="flex: 1; min-width: 280px; max-width: 380px;">
                    <div style="width: 100%; height: 320px; background: #f0f0f0; border: 2px solid #000; overflow: hidden; display: flex; align-items: center; justify-content: center; box-shadow: 4px 4px 0px #000;">
                        <img src="<%= imgSrc %>"
                             alt="<%= HtmlUtil.escape(product.getName()) %>"
                             style="width: 100%; height: 100%; object-fit: cover;"
                             onerror="this.src='${pageContext.request.contextPath}/images/default-product.svg';">
                    </div>
                </div>

                <!-- Product Info & Order Section -->
                <div style="flex: 1.2; min-width: 280px;">
                    <span class="eyebrow"><%= product.getCategory() != null ? HtmlUtil.escape(product.getCategory().toUpperCase()) : "GENERAL" %> &bull; ITEM #<%= product.getId() %></span>
                    <h1 style="font-size: 2.2rem; font-weight: 800; margin: 0.3rem 0 0.8rem; letter-spacing: -0.5px;"><%= HtmlUtil.escape(product.getName()) %></h1>

                    <div style="display: flex; align-items: baseline; gap: 12px; margin-bottom: 1.2rem;">
                        <span style="font-size: 2.2rem; font-weight: 900; color: #000;">₹<%= product.getPrice() %></span>
                        <span style="color: #666; font-size: 0.9rem;">/ unit</span>
                    </div>

                    <div style="margin-bottom: 1.5rem;">
                        <% if (inStock) { %>
                            <span class="status-badge" style="background: #2e7d32; color: #fff;">IN STOCK: <%= product.getQuantity() %> UNITS AVAILABLE</span>
                        <% } else { %>
                            <span class="status-badge" style="background: #c62828; color: #fff;">OUT OF STOCK</span>
                        <% } %>
                    </div>

                    <div style="margin-bottom: 1.8rem; border-top: 1px solid #ddd; border-bottom: 1px solid #ddd; padding: 1.2rem 0;">
                        <h4 style="font-size: 0.8rem; text-transform: uppercase; letter-spacing: 1px; color: #555; margin-bottom: 0.5rem;">Product Description</h4>
                        <p style="font-size: 0.95rem; line-height: 1.6; color: #333;">
                            <%= (product.getDescription() != null && !product.getDescription().trim().isEmpty()) ? HtmlUtil.escape(product.getDescription()) : "No detailed description provided by the seller." %>
                        </p>
                    </div>

                    <% if (inStock) { %>
                        <form action="${pageContext.request.contextPath}/buyer/cart" method="post">
                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                            <input type="hidden" name="action" value="add">
                            <input type="hidden" name="productId" value="<%= product.getId() %>">

                            <div style="display: flex; align-items: flex-end; gap: 14px; margin-bottom: 1.5rem;">
                                <div style="width: 110px;">
                                    <label for="quantity" style="display: block; font-size: 0.75rem; font-weight: 700; text-transform: uppercase; margin-bottom: 4px;">Quantity</label>
                                    <input type="number"
                                           id="quantity"
                                           name="quantity"
                                           value="1"
                                           min="1"
                                           max="<%= product.getQuantity() %>"
                                           style="width: 100%; padding: 0.75rem; border: 2px solid #000; font-size: 1rem; font-weight: 700; text-align: center;"
                                           required>
                                </div>

                                <div style="flex: 1;">
                                    <button type="submit" class="primary-button" style="padding: 0.85rem; width: 100%; box-shadow: 4px 4px 0px #000;">
                                        + Add to Cart
                                    </button>
                                </div>
                            </div>
                        </form>
                    <% } else { %>
                        <div style="padding: 1rem; background: #fff5f5; border: 1.5px solid #c0392b; color: #8b1e14; font-weight: 700; text-align: center; margin-bottom: 1.5rem;">
                            This item is currently out of stock. Please check back later!
                        </div>
                    <% } %>

                    <div style="display: flex; gap: 15px; font-size: 0.85rem; font-weight: 700;">
                        <a href="${pageContext.request.contextPath}/buyer/marketplace" style="color: #000; text-decoration: underline;">← Back to Marketplace</a>
                        <span style="color: #ccc;">|</span>
                        <a href="${pageContext.request.contextPath}/buyer/cart" style="color: #000; text-decoration: underline;">Go to My Cart →</a>
                    </div>

                </div>

            </div>

            <!-- ===================================================== -->
            <!-- CUSTOMER REVIEWS & RATINGS                              -->
            <!-- ===================================================== -->

            <div class="products-panel" style="margin-top: 2rem;">
                <div class="products-panel-header">
                    <div>
                        <h3>Customer Reviews (<%= reviews.size() %>)</h3>
                        <p>Ratings and feedback from verified buyers.</p>
                    </div>
                </div>

                <% if (reviews.isEmpty()) { %>
                    <div class="empty-state" style="padding: 2.5rem 1.5rem;">
                        <h3 style="font-size: 1.1rem;">No Reviews Yet</h3>
                        <p>Be the first to share your experience with this product.</p>
                    </div>
                <% } else { %>
                    <!-- Rating Summary -->
                    <div class="reviews-summary">
                        <div>
                            <span class="eyebrow">AVERAGE RATING</span>
                            <div style="font-size: 2rem; font-weight: 900; letter-spacing: -1px; color: #000;">
                                <%= String.format("%.1f", avgRating) %> <span style="font-size: 0.9rem; font-weight: 700; color: #666;">/ 5</span>
                            </div>
                            <div class="review-stars">
                                <% for (int s = 1; s <= 5; s++) { %>
                                    <% if (s <= Math.round(avgRating)) { %>
                                        <span class="star-filled">★</span>
                                    <% } else { %>
                                        <span class="star-empty">★</span>
                                    <% } %>
                                <% } %>
                            </div>
                        </div>
                        <div style="text-align: right;">
                            <div style="font-size: 1.6rem; font-weight: 900; color: #000;"><%= reviewCount %></div>
                            <div style="font-size: 0.75rem; text-transform: uppercase; letter-spacing: 1px; color: #666; font-weight: 700;">Reviews</div>
                        </div>
                    </div>

                    <!-- Review List -->
                    <div>
                        <% for (Review rv : reviews) { %>
                            <div class="review-card">
                                <div class="review-card-header">
                                    <span class="reviewer-name"><%= HtmlUtil.escape(rv.getBuyerName() != null ? rv.getBuyerName() : "Verified Buyer") %></span>
                                    <span class="review-date"><%= rv.getCreatedAt() != null ? HtmlUtil.escape(rv.getCreatedAt().toString().substring(0, 10)) : "Recently" %></span>
                                </div>
                                <div class="review-stars" style="margin-bottom: 0.5rem;">
                                    <% for (int s = 1; s <= 5; s++) { %>
                                        <% if (s <= rv.getRating()) { %>
                                            <span class="star-filled">★</span>
                                        <% } else { %>
                                            <span class="star-empty">★</span>
                                        <% } %>
                                    <% } %>
                                </div>
                                <p class="review-text">
                                    <%= (rv.getReviewText() != null && !rv.getReviewText().trim().isEmpty()) ? HtmlUtil.escape(rv.getReviewText()) : "No written comment provided." %>
                                </p>
                            </div>
                        <% } %>
                    </div>
                <% } %>
            </div>

            <!-- Review Submission -->
            <% if (hasPurchased && !hasReviewed) { %>
                <div class="products-panel" style="margin-top: 2rem;">
                    <div class="products-panel-header">
                        <div>
                            <h3>Write a Review</h3>
                            <p>Only buyers who have purchased this item can rate it.</p>
                        </div>
                    </div>
                    <form action="${pageContext.request.contextPath}/buyer/review" method="post" style="padding: 1.8rem;">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="productId" value="<%= product.getId() %>">
                        <div class="form-group" style="max-width: 240px;">
                            <label for="rating">Your Rating</label>
                            <select id="rating" name="rating" required>
                                <option value="">Select a rating</option>
                                <option value="1">1 Star</option>
                                <option value="2">2 Stars</option>
                                <option value="3">3 Stars</option>
                                <option value="4">4 Stars</option>
                                <option value="5">5 Stars</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="reviewText">Your Review (optional)</label>
                            <textarea id="reviewText" name="reviewText" rows="4" maxlength="500" placeholder="Share what you liked or didn't like about this product..."></textarea>
                        </div>
                        <button type="submit" class="primary-button" style="max-width: 320px;">Submit Review</button>
                    </form>
                </div>
            <% } else if (hasPurchased && hasReviewed) { %>
                <div class="products-panel" style="margin-top: 2rem;">
                    <div class="products-panel-header">
                        <div>
                            <h3>Thank You</h3>
                            <p>You have already reviewed this product. Only one review per purchase is allowed.</p>
                        </div>
                    </div>
                </div>
            <% } else { %>
                <div class="products-panel" style="margin-top: 2rem;">
                    <div class="products-panel-header">
                        <div>
                            <h3>Leave a Review</h3>
                            <p>Purchase this item to unlock the ability to rate and review it.</p>
                        </div>
                    </div>
                </div>
            <% } %>

        </div>
    </main>

    <!-- Footer -->
    <footer class="footer">
        <p>© 2026 RashikMart. All rights reserved.</p>
    </footer>

</body>
</html>
