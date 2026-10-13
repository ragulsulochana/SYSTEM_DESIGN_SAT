const mongoose = require('mongoose');

const MemberSchema = new mongoose.Schema({
  id: {
    type: String,
    required: true,
    unique: true,
    trim: true
  },
  name: {
    type: String,
    required: true,
    trim: true
  },
  email: {
    type: String,
    required: true,
    unique: true,
    trim: true,
    lowercase: true
  },
  accountStatus: {
    type: String,
    enum: ['ACTIVE', 'PENDING', 'SUSPENDED'],
    default: 'ACTIVE'
  },
  totalCheckedOut: {
    type: Number,
    default: 0,
    min: 0
  },
  country: {
    type: String,
    default: 'United States'
  },
  createdAt: {
    type: Date,
    default: Date.now
  }
});

module.exports = mongoose.model('Member', MemberSchema);
