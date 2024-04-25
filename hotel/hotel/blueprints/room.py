from flask import Blueprint, render_template, request, redirect, url_for, session, flash
from models import *
from decimal import Decimal
from datetime import datetime

bp = Blueprint("room", __name__, url_prefix="/")


@bp.route('/book', methods=["POST", "GET"])
def book():
    user_info = session.get('user_info')
    if not user_info:
        return redirect(url_for('user.login'))
    if request.args:
        check_in_date = request.args.get('check_in_date')
        check_out_date = request.args.get('check_out_date')
        room_type = request.args.get('room_type')
        if request.args.get('action') == 'calculate':
            price_per_night = Decimal(db.session.query(Room).filter_by(type=room_type).first().price)
            number_of_nights = (datetime.strptime(check_out_date, '%Y-%m-%d') - datetime.strptime(check_in_date, '%Y-%m-%d')).days
            price = price_per_night * number_of_nights
            return render_template('Book.html', price=price)
        else:
            room = db.session.query(Room).filter_by(type=room_type).first()
            if room:
                number_of_nights = (datetime.strptime(check_out_date, '%Y-%m-%d') - datetime.strptime(check_in_date, '%Y-%m-%d')).days
                price = Decimal(room.price) * number_of_nights
                if price <= 0:
                    flash('Please use correct date', 'error')
                    return render_template('Book.html')
                bill = Bill(room_id=room.id, account_id=user_info, check_in_date=check_in_date, check_out_date=check_out_date, amount=price)
                db.session.add(bill)
                room.is_available = False
                db.session.commit()
                db.session.close()
                return redirect(url_for('room.payment'))
            else:
                flash('There are no available rooms', 'error')
    return render_template('Book.html')


@bp.route('/rooms')
def rooms():
    user_info = session.get('user_info')
    if not user_info:
        return redirect(url_for('user.login'))
    rooms = {
        'Single': db.session.query(Room).filter_by(type='Single').all(),
        'Double': db.session.query(Room).filter_by(type='Double').all(),
        'Suite': db.session.query(Room).filter_by(type='Suite').all()
    }

    return render_template('Rooms.html',
                           Single_price=rooms['Single'][0].price,
                           Double_price=rooms['Double'][0].price,
                           Suite_price=rooms['Suite'][0].price,
                           Single_available_count=len(db.session.query(Room).filter_by(type='Single', is_available=True).all()),
                           Double_available_count=len(db.session.query(Room).filter_by(type='Double', is_available=True).all()),
                           Suite_available_count=len(db.session.query(Room).filter_by(type='Suite', is_available=True).all()))


@bp.route('/payment', methods=["GET", "POST"])
def payment():
    user_info = session.get('user_info')
    if not user_info:
        return redirect(url_for('user.login'))
    bill = db.session.query(Bill).filter_by(account_id=user_info, is_paid=False).first()
    if request.method == 'POST':
        bill.is_paid = True
        db.session.commit()
        db.session.close()
        return redirect(url_for('room.payresult'))
    return render_template('Payment.html', amount=bill.amount)


@bp.route('/payresult')
def payresult():
    user_info = session.get('user_info')
    if not user_info:
        return redirect(url_for('user.login'))
    return render_template('Payresult.html')
