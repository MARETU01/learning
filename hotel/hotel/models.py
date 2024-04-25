from exts import db
from enum import Enum
from sqlalchemy import text
from sqlalchemy import Enum as EnumType


class RoomType(Enum):
    Single = "Single"
    Double = "Double"
    Suite = "Suite"


class Account(db.Model):
    __tablename__ = 'account'

    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    username = db.Column(db.String(20), nullable=False)
    realname = db.Column(db.String(20), nullable=False)
    password = db.Column(db.String(20), nullable=False)
    phone = db.Column(db.String(11), nullable=False)
    is_manager = db.Column(db.Boolean, nullable=False, server_default=text('False'))


class Room(db.Model):
    __tablename__ = 'room'

    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    type = db.Column(EnumType(RoomType), nullable=False)
    price = db.Column(db.Numeric(8, 2), nullable=False)
    is_available = db.Column(db.Boolean, nullable=False, server_default=text('True'))


class Bill(db.Model):
    __tablename__ = 'bill'

    booking_id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    room_id = db.Column(db.Integer, db.ForeignKey('room.id'))
    account_id = db.Column(db.Integer, db.ForeignKey('account.id'))
    check_in_date = db.Column(db.Date, nullable=False)
    check_out_date = db.Column(db.Date, nullable=False)
    amount = db.Column(db.Numeric(8, 2), nullable=False)
    if_refund = db.Column(db.Boolean, nullable=False, server_default=text('False'))
    is_paid = db.Column(db.Boolean, nullable=False, server_default=text('False'))

    account = db.relationship('Account', backref=db.backref('bills'))
    room = db.relationship('Room', backref=db.backref('bills'))
