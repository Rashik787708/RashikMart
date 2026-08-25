<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>RashikMart - Modern Marketplace</title>
    <link rel="icon" href="data:,">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=20260824">
    <style>
        /* Specific hero & grid layout for index page */
        .hero {
            text-align: center;
            max-width: 650px;
            margin: 0 auto;
            padding: 3rem 1rem;
        }

        .hero h1 {
            font-size: 3rem;
            font-weight: 700;
            letter-spacing: -1px;
            margin-bottom: 1rem;
            text-transform: uppercase;
        }

        .hero p {
            font-size: 1.1rem;
            color: #555555;
            margin-bottom: 2rem;
            line-height: 1.6;
        }

        .hero-actions {
            display: flex;
            gap: 1rem;
            justify-content: center;
            max-width: 360px;
            margin: 0 auto;
        }

        .secondary-button {
            width: 100%;
            padding: 0.95rem;
            background-color: #ffffff;
            color: #000000;
            border: 2px solid #000000;
            border-radius: 0px;
            font-size: 0.95rem;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 1px;
            text-decoration: none;
            display: inline-block;
            text-align: center;
            transition: all 0.15s ease;
        }

        .secondary-button:hover {
            background-color: #000000;
            color: #ffffff;
        }

        .btn-link {
            text-decoration: none;
            flex: 1;
        }

        .features-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 1.5rem;
            max-width: 900px;
            width: 100%;
            margin: 3rem auto 0 auto;
        }

        .feature-card {
            border: 2px solid #000000;
            padding: 1.5rem;
            background: #ffffff;
            box-shadow: 4px 4px 0px #000000;
        }

        .feature-card h3 {
            font-size: 1.1rem;
            font-weight: 700;
            text-transform: uppercase;
            margin-bottom: 0.5rem;
        }

        .feature-card p {
            font-size: 0.9rem;
            color: #444444;
            line-height: 1.4;
        }
    </style>
</head>
<body>

    <header class="navbar">
        <a href="${pageContext.request.contextPath}/" class="brand">RashikMart</a>
        <nav>
            <ul class="nav-links">
                <li><a href="${pageContext.request.contextPath}/login.jsp" class="nav-link">Login</a></li>
                <li><a href="${pageContext.request.contextPath}/register.jsp" class="nav-link">Register</a></li>
            </ul>
        </nav>
    </header>

    <main class="page">
        <div class="hero">
            <span class="eyebrow">Welcome to RashikMart</span>
            <h1>Buy & Sell Direct</h1>
            <p>The minimal marketplace connecting buyers and sellers directly. Experience modern commerce with simplicity and speed.</p>

            <div class="hero-actions">
                <a href="${pageContext.request.contextPath}/register.jsp" class="btn-link">
                    <button type="button" class="primary-button">Get Started</button>
                </a>
                <a href="${pageContext.request.contextPath}/login.jsp" class="btn-link">
                    <span class="secondary-button">Log In</span>
                </a>
            </div>

            <div class="features-grid">
                <div class="feature-card">
                    <h3>For Buyers</h3>
                    <p>Browse products, manage your orders, and enjoy a seamless purchasing experience.</p>
                </div>
                <div class="feature-card">
                    <h3>For Sellers</h3>
                    <p>List items quickly, manage inventory, and reach customers directly.</p>
                </div>
            </div>
        </div>
    </main>

</body>
</html>