const mongoose = require('mongoose');

const BookItemSchema = new mongoose.Schema({
  barcode: {
    type: String,
    required: true,
    unique: true,
    trim: true
  },
  isbn: {
    type: String,
    required: true,
    ref: 'BookTitle'
  },
  status: {
    type: String,
    enum: ['AVAILABLE', 'RESERVED', 'LOANED', 'LOST', 'DIGITAL'],
    default: 'AVAILABLE'
  },
  /**
   * Explicit version field for Optimistic Concurrency Control:
   * Checked and incremented atomically during checkouts.
   */
  version: {
    type: Number,
    default: 0
  },
  shelfLocation: {
    type: String
  }
});

BookItemSchema.index({ isbn: 1, status: 1 });

module.exports = mongoose.model('BookItem', BookItemSchema);
