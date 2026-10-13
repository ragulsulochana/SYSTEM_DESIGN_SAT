const mongoose = require('mongoose');

const ReservationSchema = new mongoose.Schema({
  id: {
    type: String,
    unique: true
  },
  memberId: {
    type: String,
    required: true,
    ref: 'Member'
  },
  isbn: {
    type: String,
    required: true,
    ref: 'BookTitle'
  },
  date: {
    type: Date,
    default: Date.now
  },
  status: {
    type: String,
    enum: ['PENDING', 'COMPLETED', 'CANCELLED'],
    default: 'PENDING'
  }
});

ReservationSchema.index({ isbn: 1, status: 1 });
ReservationSchema.index({ memberId: 1, isbn: 1, status: 1 });

module.exports = mongoose.model('Reservation', ReservationSchema);
