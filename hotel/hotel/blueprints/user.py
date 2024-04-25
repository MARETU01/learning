from flask import Blueprint, render_template, request, redirect, url_for, flash, session
from models import *

bp = Blueprint("user", __name__, url_prefix="/")


@bp.route('/', methods=["POST", "GET"])
def login():
    if request.method == 'POST':
        username = request.form.get('username')
        password = request.form.get('password')
        user = db.session.query(Account).filter_by(username=username).first()
        if user:
            if user.password == password:
                session['user_info'] = user.id
                if user.is_manager:
                    return redirect(url_for('manager.manager'))
                return redirect(url_for('user.index'))
            flash('Password error', 'error')
        else:
            flash('The account does not exist, please register first', 'error')
    return render_template('Login.html')


@bp.route('/register', methods=["POST", "GET"])
def register():
    if request.method == 'POST':
        username = request.form['username']
        realname = request.form['realname']
        password = request.form['password']
        phone = request.form['phone']
        if Account.query.filter_by(username=username).first():
            flash('This account already exists', 'error')
            return render_template('Register.html')
        account = Account(username=username, password=password, realname=realname, phone=phone)
        db.session.add(account)
        db.session.commit()
        db.session.close()
        return redirect(url_for('user.login'))
    return render_template('Register.html')


@bp.route('/index')
def index():
    user_info = session.get('user_info')
    if not user_info:
        return redirect(url_for('user.login'))
    return render_template('Index.html')


@bp.route('/manage_booking', methods=["POST", "GET"])
def manage_booking():
    user_info = session.get('user_info')
    if not user_info:
        return redirect(url_for('user.login'))
    if request.method == 'POST':
        book_id = request.form.get('booking_id')
        book_to_handle = db.session.query(Bill).filter_by(booking_id=book_id).first()
        if book_to_handle:
            book_to_handle.if_refund = True
            room = db.session.query(Room).filter_by(id=book_to_handle.room_id).first()
            room.is_available = True
            db.session.commit()
    bills = db.session.query(Bill).filter_by(account_id=user_info).all()
    return render_template('Manage_booking.html', bills=bills)


@bp.route('/logout')
def logout():
    session.pop('user_info')
    return redirect(url_for('user.login'))
