"""Hello unit test module."""

from django_backend.hello import hello


def test_hello():
    """Test the hello function."""
    assert hello() == "Hello django-backend"
