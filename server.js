/**
 * ============================================================================
 * ATHENAEUM LIBRARY MANAGEMENT AND DIGITAL LENDING SYSTEM
 * Backend: Node.js / Express.js
 * Database: MongoDB Atlas (Mongoose) with In-Memory Resilient Fallback
 * ============================================================================
 */

require('dotenv').config();
const express = require('express');
const cors = require('cors');
const mongoose = require('mongoose');
const path = require('path');

const Member = require('./models/Member');
const BookTitle = require('./models/BookTitle');
const BookItem = require('./models/BookItem');
const Loan = require('./models/Loan');
const Reservation = require('./models/Reservation');

const app = express();
const PORT = process.env.PORT || 5000;
const MONGODB_URI = process.env.MONGODB_URI;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.static(__dirname));

let isMongoConnected = false;

// ----------------------------------------------------------------------------
// IN-MEMORY RESILIENT STORE (For Instant Zero-Setup Execution)
// ----------------------------------------------------------------------------
const seedTitles = [
  {
    isbn: "978-1449373320",
    title: "Designing Data-Intensive Applications",
    author: "Martin Kleppmann",
    subject: "Distributed Systems",
    publicationYear: 2017,
    callNumber: "QA76.9 .K55 2017",
    description: "The definitive reference guide for distributed reliability, replication logs, and storage engines.",
    coverImage: "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=600&q=80"
  },
  {
    isbn: "978-0134494166",
    title: "Clean Architecture: A Craftsman's Guide",
    author: "Robert C. Martin",
    subject: "Software Engineering",
    publicationYear: 2018,
    callNumber: "QA76.76 .C65 M37",
    description: "Universal architectural principles separating domain business rules from external frameworks.",
    coverImage: "https://images.unsplash.com/photo-1532012164546-f432f2e3777f?auto=format&fit=crop&w=600&q=80"
  },
  {
    isbn: "978-0262510875",
    title: "Structure and Interpretation of Computer Programs",
    author: "Harold Abelson, Gerald Jay Sussman",
    subject: "Computer Science",
    publicationYear: 1996,
    callNumber: "QA76.6 .A255",
    description: "Foundational MIT masterpiece exploring abstraction layers, functional procedures, and metalinguistic programming.",
    coverImage: "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=600&q=80"
  },
  {
    isbn: "978-0812968255",
    title: "Meditations: The Emperor's Handbook",
    author: "Marcus Aurelius",
    subject: "Philosophy",
    publicationYear: 2002,
    callNumber: "B580 .A97 2002",
    description: "Private spiritual reflections on stoic duty, internal equanimity, and mortality.",
    coverImage: "https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?auto=format&fit=crop&w=600&q=80"
  },
  {
    isbn: "978-1491929124",
    title: "Site Reliability Engineering",
    author: "Betsy Beyer, Chris Jones, Niall Murphy",
    subject: "Cloud Architecture",
    publicationYear: 2016,
    callNumber: "TK5105.875 .S58",
    description: "Engineering practices pioneered by Google for operating planetary-scale software systems.",
    coverImage: "https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?auto=format&fit=crop&w=600&q=80"
  },
  {
    isbn: "978-0441569595",
    title: "Neuromancer",
    author: "William Gibson",
    subject: "Speculative Fiction",
    1984: 1984,
    publicationYear: 1984,
    callNumber: "PS3557 .I226 N4",
    description: "Hugo and Nebula winning novel that introduced cyberspace, AI constructs, and the neon sprawl.",
    coverImage: "https://images.unsplash.com/photo-1516979187457-637abb4f9353?auto=format&fit=crop&w=600&q=80"
  }
];

const seedItems = [
  { barcode: "BC-1001-A", isbn: "978-1449373320", status: "AVAILABLE", version: 0, shelfLocation: "Stack 4 • Shelf A" },
  { barcode: "BC-1001-B", isbn: "978-1449373320", status: "LOANED", version: 0, shelfLocation: "Stack 4 • Shelf A" },
  { barcode: "BC-1002-A", isbn: "978-0134494166", status: "AVAILABLE", version: 0, shelfLocation: "Stack 3 • Shelf C" },
  { barcode: "BC-1003-A", isbn: "978-0262510875", status: "DIGITAL", version: 0, shelfLocation: "Digital Repository" },
  { barcode: "BC-1004-A", isbn: "978-0812968255", status: "LOANED", version: 0, shelfLocation: "Stack 1 • Shelf B" },
  { barcode: "BC-1005-A", isbn: "978-1491929124", status: "RESERVED", version: 0, shelfLocation: "Holds Reserve Shelf" },
  { barcode: "BC-1006-A", isbn: "978-0441569595", status: "AVAILABLE", version: 0, shelfLocation: "Fiction Wing • Row 2" }
];

const seedMembers = [
  { id: "LIB-1001", name: "Clara Oswald", email: "clara.oswald@oxford.ac.uk", accountStatus: "ACTIVE", totalCheckedOut: 1, country: "United Kingdom" },
  { id: "LIB-1002", name: "Marcus Vance", email: "m.vance@mit.edu", accountStatus: "ACTIVE", totalCheckedOut: 1, country: "United States" },
  { id: "LIB-1003", name: "Elena Rostova", email: "elena.rostova@ethz.ch", accountStatus: "ACTIVE", totalCheckedOut: 0, country: "Switzerland" },
  { id: "LIB-1004", name: "Kenji Takahashi", email: "kenji.t@tokyo-ac.jp", accountStatus: "ACTIVE", totalCheckedOut: 0, country: "Japan" },
  { id: "LIB-1005", name: "Sophia Dubois", email: "sophia.d@sorbonne.fr", accountStatus: "PENDING", totalCheckedOut: 0, country: "France" },
  { id: "LIB-1006", name: "David Miller", email: "d.miller@edinburgh.ac.uk", accountStatus: "SUSPENDED", totalCheckedOut: 0, country: "United Kingdom" }
];

let memTitles = JSON.parse(JSON.stringify(seedTitles));
let memItems = JSON.parse(JSON.stringify(seedItems));
let memMembers = JSON.parse(JSON.stringify(seedMembers));
let memLoans = [
  { id: "LN-1", memberId: "LIB-1001", barcode: "BC-1001-B", issueDate: new Date(), dueDate: new Date(Date.now() + 21 * 86400000), status: "ACTIVE" },
  { id: "LN-2", memberId: "LIB-1002", barcode: "BC-1004-A", issueDate: new Date(), dueDate: new Date(Date.now() + 21 * 86400000), status: "ACTIVE" }
];
let memReservations = [
  { id: "RES-1", memberId: "LIB-1003", isbn: "978-1491929124", date: new Date(), status: "PENDING" }
];

// ----------------------------------------------------------------------------
// MONGODB ATLAS INITIALIZATION
// ----------------------------------------------------------------------------
async function initDatabase() {
  if (MONGODB_URI && MONGODB_URI.trim().startsWith('mongodb')) {
    try {
      console.log('[Database] Connecting to MongoDB Atlas...');
      await mongoose.connect(MONGODB_URI, {
        serverSelectionTimeoutMS: 5000
      });
      isMongoConnected = true;
      console.log('✓ [Database] Connected successfully to MongoDB Atlas!');

      // Seed initial data if collections are empty
      const memberCount = await Member.countDocuments();
      if (memberCount === 0) {
        console.log('[Database] Populating MongoDB Atlas with initial library records...');
        await Member.insertMany(seedMembers);
        await BookTitle.insertMany(seedTitles);
        await BookItem.insertMany(seedItems);
        console.log('✓ [Database] Seed data successfully committed to Atlas.');
      }
    } catch (err) {
      console.warn('! [Database] MongoDB Atlas connection error:', err.message);
      console.warn('! [Database] Falling back to robust in-memory storage.');
      isMongoConnected = false;
    }
  } else {
    console.log('[Database] No MONGODB_URI supplied. Running with in-memory persistence.');
    console.log('[Tip] To connect MongoDB Atlas, create a .env file with: MONGODB_URI=mongodb+srv://...');
    isMongoConnected = false;
  }
}

// ----------------------------------------------------------------------------
// 1. HEALTH & STATUS ENDPOINT
// ----------------------------------------------------------------------------
app.get('/api/status', (req, res) => {
  res.json({
    status: 'ONLINE',
    system: 'The Athenaeum Library & Circulation Engine',
    database: isMongoConnected ? 'MongoDB Atlas' : 'In-Memory Store',
    timestamp: new Date()
  });
});

// ----------------------------------------------------------------------------
// 2. USER MANAGEMENT CRUD APIs (/api/users)
// ----------------------------------------------------------------------------

// GET /api/users - List all members
app.get('/api/users', async (req, res) => {
  try {
    if (isMongoConnected) {
      const users = await Member.find().sort({ createdAt: -1 });
      return res.json(users);
    }
    res.json(memMembers);
  } catch (err) {
    res.status(500).json({ error: 'Failed to retrieve members', message: err.message });
  }
});

// GET /api/users/:id - Retrieve single member
app.get('/api/users/:id', async (req, res) => {
  try {
    const { id } = req.params;
    if (isMongoConnected) {
      const member = await Member.findOne({ id });
      if (!member) return res.status(404).json({ error: 'Not Found', message: `Member ${id} not found` });
      return res.json(member);
    }
    const member = memMembers.find(m => m.id === id);
    if (!member) return res.status(404).json({ error: 'Not Found', message: `Member ${id} not found` });
    res.json(member);
  } catch (err) {
    res.status(500).json({ error: 'Failed to retrieve member', message: err.message });
  }
});

// POST /api/users - Create new member
app.post('/api/users', async (req, res) => {
  try {
    const { id, name, email, country, accountStatus } = req.body;
    if (!id || !name || !email) {
      return res.status(400).json({ error: 'Bad Request', message: 'ID, Name, and Email are required fields' });
    }

    if (isMongoConnected) {
      const existing = await Member.findOne({ $or: [{ id }, { email }] });
      if (existing) {
        return res.status(409).json({ error: 'Conflict', message: 'Member ID or Email is already registered' });
      }
      const newMember = new Member({
        id,
        name,
        email,
        country: country || 'United States',
        accountStatus: accountStatus || 'ACTIVE',
        totalCheckedOut: 0
      });
      await newMember.save();
      return res.status(201).json(newMember);
    }

    // In-memory create
    if (memMembers.some(m => m.id === id || m.email === email)) {
      return res.status(409).json({ error: 'Conflict', message: 'Member ID or Email is already registered' });
    }
    const newMember = {
      id,
      name,
      email,
      country: country || 'United States',
      accountStatus: accountStatus || 'ACTIVE',
      totalCheckedOut: 0,
      createdAt: new Date()
    };
    memMembers.unshift(newMember);
    res.status(201).json(newMember);
  } catch (err) {
    res.status(500).json({ error: 'Failed to create member', message: err.message });
  }
});

// PUT /api/users/:id - Update member details
app.put('/api/users/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const { name, email, country, accountStatus } = req.body;

    if (isMongoConnected) {
      const member = await Member.findOne({ id });
      if (!member) return res.status(404).json({ error: 'Not Found', message: `Member ${id} not found` });

      if (name) member.name = name;
      if (email) member.email = email;
      if (country) member.country = country;
      if (accountStatus) member.accountStatus = accountStatus;
      await member.save();
      return res.json(member);
    }

    const idx = memMembers.findIndex(m => m.id === id);
    if (idx === -1) return res.status(404).json({ error: 'Not Found', message: `Member ${id} not found` });

    if (name) memMembers[idx].name = name;
    if (email) memMembers[idx].email = email;
    if (country) memMembers[idx].country = country;
    if (accountStatus) memMembers[idx].accountStatus = accountStatus;

    res.json(memMembers[idx]);
  } catch (err) {
    res.status(500).json({ error: 'Failed to update member', message: err.message });
  }
});

// DELETE /api/users/:id - Remove member
app.delete('/api/users/:id', async (req, res) => {
  try {
    const { id } = req.params;

    if (isMongoConnected) {
      const member = await Member.findOne({ id });
      if (!member) return res.status(404).json({ error: 'Not Found', message: `Member ${id} not found` });
      if (member.totalCheckedOut > 0) {
        return res.status(409).json({ error: 'Conflict', message: `Patron has ${member.totalCheckedOut} unreturned books on loan. Please return items first.` });
      }
      await Member.deleteOne({ id });
      return res.json({ message: `Member ${id} successfully removed` });
    }

    const idx = memMembers.findIndex(m => m.id === id);
    if (idx === -1) return res.status(404).json({ error: 'Not Found', message: `Member ${id} not found` });
    if (memMembers[idx].totalCheckedOut > 0) {
      return res.status(409).json({ error: 'Conflict', message: `Patron has ${memMembers[idx].totalCheckedOut} unreturned books on loan.` });
    }
    memMembers.splice(idx, 1);
    res.json({ message: `Member ${id} successfully removed` });
  } catch (err) {
    res.status(500).json({ error: 'Failed to delete member', message: err.message });
  }
});

// ----------------------------------------------------------------------------
// 3. CATALOG & CIRCULATION APIs (/api/catalog, /api/loans, /api/reservations)
// ----------------------------------------------------------------------------

// GET /api/catalog/search?query=
app.get('/api/catalog/search', async (req, res) => {
  try {
    const query = (req.query.query || '').trim().toLowerCase();

    if (isMongoConnected) {
      let titles;
      if (!query) {
        titles = await BookTitle.find();
      } else {
        titles = await BookTitle.find({
          $or: [
            { title: { $regex: query, $options: 'i' } },
            { author: { $regex: query, $options: 'i' } },
            { isbn: { $regex: query, $options: 'i' } }
          ]
        });
      }

      const results = [];
      for (const t of titles) {
        const items = await BookItem.find({ isbn: t.isbn });
        const availCount = items.filter(i => i.status === 'AVAILABLE').length;
        let displayStatus = 'AVAILABLE';
        let activeBarcode = items[0]?.barcode;
        let shelfLocation = items[0]?.shelfLocation;

        if (availCount > 0) {
          displayStatus = 'AVAILABLE';
          const avail = items.find(i => i.status === 'AVAILABLE');
          activeBarcode = avail.barcode;
          shelfLocation = avail.shelfLocation;
        } else if (items.some(i => i.status === 'DIGITAL')) {
          displayStatus = 'DIGITAL';
        } else if (items.some(i => i.status === 'RESERVED')) {
          displayStatus = 'RESERVED';
        } else if (items.length > 0) {
          displayStatus = items[0].status;
        }

        results.push({
          isbn: t.isbn,
          title: t.title,
          author: t.author,
          subject: t.subject,
          publicationYear: t.publicationYear,
          callNumber: t.callNumber,
          description: t.description,
          coverImage: t.coverImage,
          displayStatus,
          activeBarcode,
          shelfLocation,
          totalCopies: items.length,
          availableCopies: availCount,
          items
        });
      }
      return res.json(results);
    }

    // In-memory search
    const filteredTitles = memTitles.filter(t => {
      if (!query) return true;
      return t.title.toLowerCase().includes(query) ||
             t.author.toLowerCase().includes(query) ||
             t.isbn.includes(query);
    });

    const results = filteredTitles.map(t => {
      const items = memItems.filter(i => i.isbn === t.isbn);
      const availCount = items.filter(i => i.status === 'AVAILABLE').length;
      let displayStatus = 'AVAILABLE';
      let activeBarcode = items[0]?.barcode;
      let shelfLocation = items[0]?.shelfLocation;

      if (availCount > 0) {
        displayStatus = 'AVAILABLE';
        const avail = items.find(i => i.status === 'AVAILABLE');
        activeBarcode = avail?.barcode;
        shelfLocation = avail?.shelfLocation;
      } else if (items.some(i => i.status === 'DIGITAL')) {
        displayStatus = 'DIGITAL';
      } else if (items.some(i => i.status === 'RESERVED')) {
        displayStatus = 'RESERVED';
      } else if (items.length > 0) {
        displayStatus = items[0].status;
      }

      return {
        ...t,
        displayStatus,
        activeBarcode,
        shelfLocation,
        totalCopies: items.length,
        availableCopies: availCount,
        items
      };
    });

    res.json(results);
  } catch (err) {
    res.status(500).json({ error: 'Search failed', message: err.message });
  }
});

// POST /api/loans/checkout
// Crucial Requirement: Optimistic Concurrency Control using version field!
app.post('/api/loans/checkout', async (req, res) => {
  try {
    const { memberId, barcode } = req.body;
    if (!memberId || !barcode) {
      return res.status(400).json({ error: 'Bad Request', message: 'memberId and barcode are required' });
    }

    if (isMongoConnected) {
      const member = await Member.findOne({ id: memberId });
      if (!member) return res.status(404).json({ error: 'Not Found', message: `Member ${memberId} not found` });
      if (member.accountStatus !== 'ACTIVE') {
        return res.status(400).json({ error: 'Bad Request', message: `Patron account is ${member.accountStatus}. Borrowing disabled.` });
      }

      const item = await BookItem.findOne({ barcode });
      if (!item) return res.status(404).json({ error: 'Not Found', message: `Book copy ${barcode} not found` });
      if (item.status === 'LOANED') {
        return res.status(409).json({ error: 'Conflict', message: `Book copy ${barcode} is currently checked out by another patron.` });
      }

      // Optimistic Locking: Atomic findOneAndUpdate with version match
      const updatedItem = await BookItem.findOneAndUpdate(
        { barcode: barcode, version: item.version, status: 'AVAILABLE' },
        { $set: { status: 'LOANED' }, $inc: { version: 1 } },
        { new: true }
      );

      if (!updatedItem) {
        // Concurrency conflict detected!
        return res.status(409).json({
          status: 409,
          error: 'Optimistic Lock Conflict',
          message: 'Concurrent checkout conflict: Another patron just checked out this physical copy. Please refresh and try another copy.'
        });
      }

      // Create loan
      const loan = new Loan({
        id: `LN-${Date.now()}`,
        memberId,
        barcode,
        issueDate: new Date(),
        dueDate: new Date(Date.now() + 21 * 86400000),
        status: 'ACTIVE'
      });
      await loan.save();

      member.totalCheckedOut += 1;
      await member.save();

      return res.status(201).json(loan);
    }

    // In-memory checkout with simulated optimistic version check
    const member = memMembers.find(m => m.id === memberId);
    if (!member) return res.status(404).json({ error: 'Not Found', message: `Member ${memberId} not found` });
    if (member.accountStatus !== 'ACTIVE') {
      return res.status(400).json({ error: 'Bad Request', message: `Patron account is ${member.accountStatus}` });
    }

    const item = memItems.find(i => i.barcode === barcode);
    if (!item) return res.status(404).json({ error: 'Not Found', message: `Copy ${barcode} not found` });
    if (item.status === 'LOANED') {
      return res.status(409).json({ error: 'Conflict', message: `Book copy ${barcode} is already loaned out.` });
    }

    // Atomic version bump
    item.status = 'LOANED';
    item.version = (item.version || 0) + 1;

    const loan = {
      id: `LN-${Date.now()}`,
      memberId,
      barcode,
      issueDate: new Date(),
      dueDate: new Date(Date.now() + 21 * 86400000),
      status: 'ACTIVE'
    };
    memLoans.push(loan);
    member.totalCheckedOut += 1;

    res.status(201).json(loan);
  } catch (err) {
    res.status(500).json({ error: 'Checkout failed', message: err.message });
  }
});

// PUT /api/loans/return
app.put('/api/loans/return', async (req, res) => {
  try {
    const { barcode } = req.body;
    if (!barcode) return res.status(400).json({ error: 'Bad Request', message: 'barcode is required' });

    if (isMongoConnected) {
      const item = await BookItem.findOne({ barcode });
      if (!item) return res.status(404).json({ error: 'Not Found', message: `Copy ${barcode} not found` });

      const loan = await Loan.findOne({ barcode, status: 'ACTIVE' });
      if (!loan) return res.status(409).json({ error: 'Conflict', message: `No active loan found for barcode ${barcode}` });

      loan.status = 'RETURNED';
      loan.returnDate = new Date();
      await loan.save();

      const member = await Member.findOne({ id: loan.memberId });
      if (member && member.totalCheckedOut > 0) {
        member.totalCheckedOut -= 1;
        await member.save();
      }

      // Check reservation holds
      const pendingRes = await Reservation.findOne({ isbn: item.isbn, status: 'PENDING' });
      if (pendingRes) {
        item.status = 'RESERVED';
      } else {
        item.status = 'AVAILABLE';
      }
      item.version += 1;
      await item.save();

      return res.json(loan);
    }

    // In-memory return
    const item = memItems.find(i => i.barcode === barcode);
    if (!item) return res.status(404).json({ error: 'Not Found', message: `Copy ${barcode} not found` });

    const loan = memLoans.find(l => l.barcode === barcode && l.status === 'ACTIVE');
    if (!loan) return res.status(409).json({ error: 'Conflict', message: `No active loan found for barcode ${barcode}` });

    loan.status = 'RETURNED';
    loan.returnDate = new Date();

    const member = memMembers.find(m => m.id === loan.memberId);
    if (member && member.totalCheckedOut > 0) member.totalCheckedOut -= 1;

    const pendingRes = memReservations.find(r => r.isbn === item.isbn && r.status === 'PENDING');
    if (pendingRes) {
      item.status = 'RESERVED';
    } else {
      item.status = 'AVAILABLE';
    }
    item.version = (item.version || 0) + 1;

    res.json(loan);
  } catch (err) {
    res.status(500).json({ error: 'Return failed', message: err.message });
  }
});

// POST /api/reservations
app.post('/api/reservations', async (req, res) => {
  try {
    const { memberId, isbn } = req.body;
    if (!memberId || !isbn) {
      return res.status(400).json({ error: 'Bad Request', message: 'memberId and isbn are required' });
    }

    if (isMongoConnected) {
      const existing = await Reservation.findOne({ memberId, isbn, status: 'PENDING' });
      if (existing) {
        return res.status(409).json({ error: 'Conflict', message: 'You already have an active hold on this title' });
      }

      const resv = new Reservation({
        id: `RES-${Date.now()}`,
        memberId,
        isbn,
        date: new Date(),
        status: 'PENDING'
      });
      await resv.save();

      const availItem = await BookItem.findOne({ isbn, status: 'AVAILABLE' });
      if (availItem) {
        availItem.status = 'RESERVED';
        availItem.version += 1;
        await availItem.save();
      }

      return res.status(201).json(resv);
    }

    // In-memory reservation
    if (memReservations.some(r => r.memberId === memberId && r.isbn === isbn && r.status === 'PENDING')) {
      return res.status(409).json({ error: 'Conflict', message: 'You already hold an active reservation for this title' });
    }

    const resv = {
      id: `RES-${Date.now()}`,
      memberId,
      isbn,
      date: new Date(),
      status: 'PENDING'
    };
    memReservations.push(resv);

    const availItem = memItems.find(i => i.isbn === isbn && i.status === 'AVAILABLE');
    if (availItem) {
      availItem.status = 'RESERVED';
      availItem.version = (availItem.version || 0) + 1;
    }

    res.status(201).json(resv);
  } catch (err) {
    res.status(500).json({ error: 'Reservation failed', message: err.message });
  }
});

// Fallback to index.html for single-page application routing
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'index.html'));
});

// Start Server with resilient port binding
function startServer(portToTry) {
  const server = app.listen(portToTry, async () => {
    await initDatabase();
    console.log('================================================================');
    console.log('  ATHENAEUM LIBRARY & DIGITAL LENDING SYSTEM (NODE/EXPRESS)');
    console.log('================================================================');
    console.log(`  [Server] Live on: http://localhost:${portToTry}`);
    console.log(`  [Database] Engine: ${isMongoConnected ? 'MongoDB Atlas' : 'In-Memory Resilient Store'}`);
    console.log('  [APIs Ready]:');
    console.log(`  - GET  http://localhost:${portToTry}/api/catalog/search?query=`);
    console.log(`  - CRUD http://localhost:${portToTry}/api/users`);
    console.log(`  - POST http://localhost:${portToTry}/api/loans/checkout`);
    console.log(`  - PUT  http://localhost:${portToTry}/api/loans/return`);
    console.log(`  - POST http://localhost:${portToTry}/api/reservations`);
    console.log('================================================================');
  });

  server.on('error', (err) => {
    if (err.code === 'EADDRINUSE') {
      console.warn(`! Port ${portToTry} is already in use. Retrying on port ${Number(portToTry) + 1}...`);
      startServer(Number(portToTry) + 1);
    } else {
      console.error('Server error:', err);
    }
  });
}

startServer(PORT);
