**BabyNest REST API**

**Task 1: Order Documentation & Reporting**
Retrieve detailed order summaries and generate PDF invoices in multiple formats, JSON, XML, YAML, TSV, for a given order. 

## Order Retrieval API Endpoints
| HTTP Method | URL                      | Description                    | Request Body |
|------------|--------------------------|--------------------------------|---------------|
| `GET`      | `/api/orders/{orderId}`      | Get details of a specific order (Admin only)     | ❌ No body |
| `GET`      | `/api/orders`                | Get all orders (Admin only)                      | ❌ No body |
| `GET`      | `/api/orders/{orderId}/invoice` | Generate PDF invoice for an order (Admin only)   | ❌ No body |
| `GET`      | `/api/orders?page={num}`     | Go to next page   | ❌ No body |
| `GET`      | `/api/orders?page={num}&size={num}`     | Go to next page and specifying the number of orders per page to display   | ❌ No body |

This is Role-based controlled for ADMIN Access.

---

**Task 2: Order Management**
Allow authenticated clients to place and edit orders.
- A new order is inserted into the orders table with customer details, total amount, shipping address and an estimated delivery date.
- Purchased items are added to the order_items table with product details, quantity and price.
- Payment information is stored in the payments table.
- The customers shopping cart is cleared when the order is placed to avoid duplicate checkouts.


## Order Management API Endpoints
| HTTP Method | URL                      | Description                    | Request Body |
|------------|--------------------------|--------------------------------|---------------|
| `POST`     | `/api/orders/place`     | Place an order                 | ✅ `{ "orderItems": [ { "productName": "Product A", "quantity": 1 }], "paymentMethod": "Credit Card"}` |
| `PUT`      | `/api/orders/edit{orderId}` | Edit an existing order  (only owned by user)   | ✅ `{ "orderItems": [ { "productName": "Product A", "quantity": 1 }]}` |

This is Role-based controlled for authenticated users, CUSTOMER and or ADMIN.

---

**Task 3: Unique Feature**
**Wishlist Management**
Authenticated users are able to manage their wishlist items. In addition to standard CRUD Operations, the system automatically sends an email notification each time an item is added to the wishlist. This email not only informs the user of the update but also includes a clickable link and a QR code. The QR code contains a tokenized URL that, when scanned, takes the user to a public view of their wishlist in a  styled HTML page.
  
- Email Notifications
- QR Codes
- Role-Based Access
- Pagination Support
- JWT Token used to create a public link for inserting into the QR Code.

## Wishlist API Endpoints

| HTTP Method | URL                      | Description                                    | Request Body |
|------------|--------------------------|--------------------------------|---------------|
| `GET`      | `/api/wishlist`          | Get all wishlist items for the authenticated user | ❌ No body |
| `POST`     | `/api/wishlist`          | Add an item to the wishlist    | ✅ `{ "productName": "Product", "note": "Want this for later." }` |
| `PUT`      | `/api/wishlist/{itemId}` | Update an existing wishlist item | ✅ `{ "note": "Updated note" }` |
| `DELETE`   | `/api/wishlist/{itemId}` | Remove an item from the wishlist | ❌ No body |
| `GET`      | `/api/wishlist?page={num}&size={num}`     | Go to next page and specifying the number of orders per page to display   | ❌ No body |
| `GET`      | `/public/wishlist?token={token}`     | Returns a styled HTML view of the users wishlist by validating a temporary token.   | ❌ No body |

This is Role-based controlled for authenticated users, CUSTOMER and or ADMIN.

---

**Task 4: Surprise Feature**
**Customer Details**

  - Returns in JSON
  - Includes links section using HATEOAS.
  - If customers account is not older than 5 minutes, "update-phone" link is included.
  - Can only be updated if account is not older than 5 minutes.
  - New phone number cannot be the same as the old one.
  - If the customers account is older than 5 minutes, the endpoint returns a 400 Bad Request.
  - If the phone number is the same as the old one, the endpoint returns a 400 Bad Request.
  - Customer phone number is partially masked. 

## Customer API Endpoints

| HTTP Method | URL                                       | Description                                         | Request Body |
|------------|------------------------------------------|-----------------------------------------------------|--------------|
| `GET`      | `/api/customers/{userId}`               | Retrieve customer details in JSON format, including HATEOAS links. | ❌ No body |
| `PUT`      | `/api/customers/{userId}/update-phone?newPhone=` | Update the customer's phone number (Only if the account is less than 5 minutes old) | ❌ No body |

---

**Technologies**
**Security:** Spring Security with JWT
**PDF Generation:** OpenPDF
**Data Formats:** Jackson (for JSON, XML, YAML)
**Pagination:** Spring Data Pageable
**SSL (HTTPS):** Java Keytool
**QR Code Generation:** ZXing
**Email Service:** Spring Boot Mail
**URL Forwarding:** Ngrok

