# Athenaeum: Library Management & Digital Lending System

An end-to-end Library Management and Circulation System crafted with:
* **Frontend**: HTML5, Vanilla JavaScript, Tailwind CSS (Handcrafted Editorial UI/UX)
* **Backend**: Node.js & Express.js RESTful API
* **Database**: MongoDB Atlas (via Mongoose) with resilient in-memory storage fallback

---

## 1. Project Directory Structure

```text
system design/
├── package.json               # Node.js dependencies (express, mongoose, cors, dotenv)
├── server.js                  # Express.js REST server & static host
├── .env.example               # Environment template for MongoDB Atlas URI
├── README.md                  # System architecture, API documentation, and setup guide
├── index.html                 # Handcrafted editorial web application
├── models/
│   ├── Member.js              # Mongoose model: Patron credentials and loan counters
│   ├── BookTitle.js           # Mongoose model: Bibliographic records (ISBN, Title, Author)
│   ├── BookItem.js            # Mongoose model: Physical copy with optimistic locking version
│   ├── Loan.js                # Mongoose model: Active and returned checkout records
│   └── Reservation.js         # Mongoose model: Hold reservation queues
```

---

## 2. Core Architecture & Concurrency Design

### Optimistic Concurrency Control (OCC)
In busy library branches, multiple patrons or automated catalog tasks might attempt to checkout the exact same physical copy (`BookItem`) simultaneously.

To ensure safety:
1. Each `BookItem` contains a monotonic `version` counter:
   ```javascript
   const BookItemSchema = new mongoose.Schema({
     barcode: { type: String, unique: true },
     isbn: { type: String, ref: 'BookTitle' },
     status: { type: String, enum: ['AVAILABLE', 'RESERVED', 'LOANED', 'LOST', 'DIGITAL'] },
     version: { type: Number, default: 0 }
   });
   ```
2. During checkout, an atomic query matches both `barcode`, `version`, and `status === 'AVAILABLE'`:
   ```javascript
   const updatedItem = await BookItem.findOneAndUpdate(
     { barcode: barcode, version: item.version, status: 'AVAILABLE' },
     { $set: { status: 'LOANED' }, $inc: { version: 1 } },
     { new: true }
   );
   ```
3. If two concurrent transactions execute simultaneously, the first commit increments the version from `0` to `1`. The second transaction finds 0 matching documents and immediately responds with **`HTTP 409 Conflict`**:
   ```json
   {
     "status": 409,
     "error": "Optimistic Lock Conflict",
     "message": "Concurrent checkout conflict: Another patron just checked out this physical copy. Please refresh and try another copy."
   }
   ```
4. The frontend intercepts `409` and displays a dedicated red concurrency notification toast.

---

## 3. MongoDB Atlas Schema & Collections

| Collection | Key Attributes | Notes |
|---|---|---|
| `members` | `id`, `name`, `email` (unique), `accountStatus` (`ACTIVE`, `PENDING`, `SUSPENDED`), `totalCheckedOut`, `country` | Patron management |
| `booktitles` | `isbn` (unique), `title`, `author`, `subject`, `publicationYear`, `callNumber`, `coverImage`, `description` | Text-indexed catalog |
| `bookitems` | `barcode` (unique), `isbn`, `status` (`AVAILABLE`, `LOANED`, `RESERVED`, `DIGITAL`), `version`, `shelfLocation` | Inventory & OCC |
| `loans` | `id`, `memberId`, `barcode`, `issueDate`, `dueDate`, `returnDate`, `status` (`ACTIVE`, `RETURNED`) | Circulation tracking |
| `reservations` | `id`, `memberId`, `isbn`, `date`, `status` (`PENDING`, `COMPLETED`, `CANCELLED`) | Hold queue |

---

## 4. REST API Specification

### User Management (Admin CRUD)
| Method | Endpoint | Description | Payload / Query |
|---|---|---|---|
| `GET` | `/api/users` | List all library patrons | None |
| `GET` | `/api/users/:id` | Get single patron by ID | Path parameter |
| `POST` | `/api/users` | Enroll new patron | `{ "id": "LIB-1008", "name": "Jane Foster", "email": "jane@mit.edu", "country": "US", "accountStatus": "ACTIVE" }` |
| `PUT` | `/api/users/:id` | Update patron details | `{ "name": "Jane Foster", "accountStatus": "ACTIVE" }` |
| `DELETE` | `/api/users/:id` | Remove patron | Checks that patron has 0 unreturned loans |

### Catalog & Circulation
| Method | Endpoint | Description | Payload / Query |
|---|---|---|---|
| `GET` | `/api/catalog/search` | Search catalog by title/author/ISBN | `?query=Architecture` |
| `POST` | `/api/loans/checkout` | Check out a copy with **Optimistic Locking** | `{ "memberId": "LIB-1001", "barcode": "BC-1001-A" }` |
| `PUT` | `/api/loans/return` | Return physical item to shelf | `{ "barcode": "BC-1001-A" }` |
| `POST` | `/api/reservations` | Place title on hold queue | `{ "memberId": "LIB-1001", "isbn": "978-1449373320" }` |
| `GET` | `/api/status` | System health & active DB engine | None |

---

## 5. How to Run Locally

### Step 1: Install Dependencies
```bash
npm install
```

### Step 2: (Optional) Connect MongoDB Atlas
Create a `.env` file in the project root:
```env
PORT=5000
MONGODB_URI=mongodb+srv://<username>:<password>@cluster0.mongodb.net/athenaeum_library?retryWrites=true&w=majority
```
> **Note**: If `MONGODB_URI` is omitted or cluster connection is unavailable, the server runs automatically in resilient in-memory mode with pre-seeded books and patrons so you can test immediately with zero external setup.

### Step 3: Start the Application
```bash
# Production mode
npm start

# Development live-reload mode
npm run dev
```

Open your browser to:
**`http://localhost:5000`**

---

## 6. Testing Concurrent Checkout with curl

1. **Successful Checkout (HTTP 201 Created)**:
   ```bash
   curl -X POST "http://localhost:5000/api/loans/checkout" \
     -H "Content-Type: application/json" \
     -d '{"memberId": "LIB-1001", "barcode": "BC-1001-A"}'
   ```

2. **Concurrent Collision (HTTP 409 Conflict)**:
   ```bash
   curl -X POST "http://localhost:5000/api/loans/checkout" \
     -H "Content-Type: application/json" \
     -d '{"memberId": "LIB-1002", "barcode": "BC-1001-A"}'
   ```
   **Output**:
   ```json
   {
     "status": 409,
     "error": "Conflict",
     "message": "Book copy BC-1001-A is already loaned out."
   }
   ```
