import unittest
from app import app
from models import db, Account

class TestApp(unittest.TestCase):

    def setUp(self):
        self.client = app.test_client()
        self.app_context = app.app_context()
        self.app_context.push()
        db.create_all()

    def tearDown(self):
        self.delete_test_account()
        db.session.remove()
        self.app_context.pop()

    def test_user_login(self):
        test_user = Account(username='testuser', realname='Test User', password='password', phone='1234567890', is_manager=False)
        db.session.add(test_user)
        db.session.commit()
        response = self.client.post('/', data={'username': 'testuser', 'password': 'password'}, follow_redirects=True)
        self.assertEqual(200, response.status_code)
        urls = ['/reg', '/manage_booking', '/book', '/rooms']
        with self.client.session_transaction() as sess:
            sess['user_info'] = test_user.id
        for url in urls:
            response = self.client.get(url)
            self.assertEqual(200, response.status_code)

    def delete_test_account(self):
        test_user = db.session.query(Account).filter_by(username='testuser').first()
        db.session.delete(test_user)
        db.session.commit()

if __name__ == '__main__':
    unittest.main()