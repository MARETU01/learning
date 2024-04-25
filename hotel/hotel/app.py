from flask import *
from flask_migrate import Migrate
import config
from models import Room, Account
from blueprints.manager import bp as manager_bp
from blueprints.room import bp as room_bp
from blueprints.user import bp as user_bp
from exts import db

app = Flask(__name__)
app.config.from_object(config)

db.init_app(app)
with app.app_context():
    db.create_all()
    for i in range(10):
        if not db.session.query(Room).filter_by(id=i + 1, type='Single').first():
            add_room = Room(type='Single', price=100)
            db.session.add(add_room)
    for i in range(10):
        if not db.session.query(Room).filter_by(id=i + 11, type='Double').first():
            add_room = Room(type='Double', price=200)
            db.session.add(add_room)
    for i in range(10):
        if not db.session.query(Room).filter_by(id=i + 21, type='Suite').first():
            add_room = Room(type='Suite', price=300)
            db.session.add(add_room)
    if not db.session.query(Account).filter_by(realname='root', is_manager=True).first():
        add_admin = Account(username='root', password=123456, realname='root', phone='00000000000', is_manager=True)
        db.session.add(add_admin)
    db.session.commit()

app.secret_key = 'asdbjkvbcqkejdbv'

app.register_blueprint(room_bp)
app.register_blueprint(user_bp)
app.register_blueprint(manager_bp)

migrate = Migrate(app, db)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=80, debug=True)
