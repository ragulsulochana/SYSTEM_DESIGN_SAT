const mongoose = require('mongoose');

const BookTitleSchema = new mongoose.Schema({
  isbn: {
    type: String,
    required: true,
    unique: true,
    trim: true
  },
  title: {
    type: String,
    required: true,
    trim: true
  },
  author: {
    type: String,
    required: true,
    trim: true
  },
  subject: {
    type: String,
    required: true,
    trim: true
  },
  publicationYear: {
    type: Number
  },
  callNumber: {
    type: String
  },
  description: {
    type: String
  },
  coverImage: {
    type: String
  }
});

// Compound text index for fast title, author, and ISBN searching
BookTitleSchema.index({ title: 'text', author: 'text', isbn: 'text' });

module.exports = mongoose.model('BookTitle', BookTitleSchema);
