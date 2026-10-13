const mongoose = require('mongoose');

const LoanSchema = new mongoose.Schema({
  id: {
    type: String,
    unique: true
  },
  memberId: {
    type: String,
    required: true,
    ref: 'Member'
  },
  barcode: {
    type: String,
    required: true,
    ref: 'BookItem'
  },
  issueDate: {
    type: Date,
    default: Date.now
  },
  dueDate: {
    type: Date,
    required: true
  },
  returnDate: {
    type: Date
  },
  status: {
    type: String,
    enum: ['ACTIVE', 'RETURNED', 'OVERDUE'],
    default: 'ACTIVE'
  }
});

LoanSchema.index({ barcode: 1, status: 1 });
LoanSchema.index({ memberId: 1, status: 1 });

module.exports = mongoose.model('Loan', LoanSchema);
