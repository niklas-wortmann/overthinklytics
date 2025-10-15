"""Database router for analytics models.

Routes analytics models to the 'analytics' database (Prisma's dev.db),
while keeping Django's built-in models in the default database.
"""


class AnalyticsRouter:
    """Route analytics models to the analytics database."""

    analytics_models = {
        "kpisnapshot",
        "trafficdaily",
        "signupbychannel",
        "revenuedaily",
        "deviceshare",
    }

    def db_for_read(self, model, **hints):
        """Route reads of analytics models to analytics database."""
        if model._meta.model_name in self.analytics_models:
            return "analytics"
        return None

    def db_for_write(self, model, **hints):
        """Route writes of analytics models to analytics database."""
        if model._meta.model_name in self.analytics_models:
            return "analytics"
        return None

    def allow_relation(self, obj1, obj2, **hints):
        """Allow relations if both models are in the same database."""
        db1 = (
            "analytics"
            if obj1._meta.model_name in self.analytics_models
            else "default"
        )
        db2 = (
            "analytics"
            if obj2._meta.model_name in self.analytics_models
            else "default"
        )
        return db1 == db2

    def allow_migrate(self, db, app_label, model_name=None, **hints):
        """Prevent migrations for analytics models (Prisma manages them)."""
        if model_name in self.analytics_models:
            return db == "analytics"
        return db == "default"
