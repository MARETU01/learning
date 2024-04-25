from flask import Blueprint, render_template, request, redirect, url_for, session
from models import *
import time
from datetime import date

bp = Blueprint("manager", __name__, url_prefix="/manager")


@bp.route('/')
def manager():
    user_info = session.get('user_info')
    if not user_info:
        return redirect(url_for('user.login'))
    if not db.session.query(Account).filter_by(id=user_info).first().is_manager:
        return redirect(url_for('user.index'))
    return render_template('Manager_index.html')


@bp.route('/rooms', methods=["POST", "GET"])
def rooms_manage():
    user_info = session.get('user_info')
    if not user_info:
        return redirect(url_for('user.login'))
    if not db.session.query(Account).filter_by(id=user_info).first().is_manager:
        return redirect(url_for('user.index'))
    rooms = {
        'Single': db.session.query(Room).filter_by(type='Single').all(),
        'Double': db.session.query(Room).filter_by(type='Double').all(),
        'Suite': db.session.query(Room).filter_by(type='Suite').all()
    }
    if request.method == 'POST':
        single_price = request.form.get('type1')
        double_price = request.form.get('type2')
        suite_price = request.form.get('type3')
        for room in rooms['Single']:
            room.price = single_price
        for room in rooms['Double']:
            room.price = double_price
        for room in rooms['Suite']:
            room.price = suite_price
        db.session.commit()
    return render_template('Manager_rooms.html',
                           quantity1=len(db.session.query(Room).filter_by(type='Single', is_available=True).all()),
                           quantity2=len(db.session.query(Room).filter_by(type='Double', is_available=True).all()),
                           quantity3=len(db.session.query(Room).filter_by(type='Suite', is_available=True).all()),
                           Original_price1=rooms['Single'][0].price,
                           Original_price2=rooms['Double'][0].price,
                           Original_price3=rooms['Suite'][0].price)


@bp.route('/refund', methods=["POST", "GET"])
def refund_manage():
    user_info = session.get('user_info')
    if not user_info:
        return redirect(url_for('user.login'))
    account = db.session.query(Account).filter_by(id=user_info).first()
    if not account.is_manager:
        return redirect(url_for('user.index'))
    if request.method == 'POST':
        bill_id = request.form.get('booking_id')
        bill = db.session.query(Bill).filter_by(booking_id=bill_id, if_refund=True).first()
        if bill:
            db.session.delete(bill)
            db.session.commit()
            db.session.close()
    bills = db.session.query(Bill).filter_by(if_refund=True).all()
    return render_template('Refund.html', bills=bills)


@bp.route('/bill', methods=["POST", "GET"])
def bill_manage():
    user_info = session.get('user_info')
    if not user_info:
        return redirect(url_for('user.login'))
    if not db.session.query(Account).filter_by(id=user_info).first().is_manager:
        return redirect(url_for('user.index'))
    now_time = time.localtime()
    now_date = date(now_time.tm_year, now_time.tm_mon, now_time.tm_mday)
    bills = db.session.query(Bill).filter_by(check_in_date=now_date).all()
    single_number = double_number = suite_number = 0
    for bill in bills:
        if bill.room.type == "Single":
            single_number += 1
        elif bill.room.type == "Double":
            double_number += 1
        elif bill.room.type == "Suite":
            suite_number += 1
    return render_template('Manager_bill.html',
                           bills=bills,
                           quantity1=single_number,
                           quantity2=double_number,
                           quantity3=suite_number)
