from django.db import IntegrityError
from django_backend.models import DeviceShare
from django_backend.serializers import DeviceShareResponseSerializer, DeviceSharePointSerializer
from django_backend.tests import BaseTestCase


class DeviceShareModelTest(BaseTestCase):
    """Tests for DeviceShare model and manager."""

    def setUp(self):
        """Create test data for DeviceShare model."""
        DeviceShare.objects.create(snapshotdate=20251020, device="Desktop", sharepct=45.5)
        DeviceShare.objects.create(snapshotdate=20251020, device="Mobile", sharepct=40.0)
        DeviceShare.objects.create(snapshotdate=20251020, device="Tablet", sharepct=14.5)
        DeviceShare.objects.create(snapshotdate=20251021, device="Desktop", sharepct=46.0)
        DeviceShare.objects.create(snapshotdate=20251021, device="Mobile", sharepct=39.5)
        DeviceShare.objects.create(snapshotdate=20251021, device="Tablet", sharepct=14.5)

    def test_device_share_creation(self):
        """Test that DeviceShare objects can be created correctly."""
        device = DeviceShare.objects.get(snapshotdate=20251020, device="Desktop")
        self.assertEqual(device.snapshotdate, 20251020)
        self.assertEqual(device.device, "Desktop")
        self.assertEqual(device.sharepct, 45.5)

    def test_unique_together_constraint(self):
        """Test that unique_together constraint is enforced."""
        with self.assertRaises(IntegrityError):
            DeviceShare.objects.create(snapshotdate=20251020, device="Desktop", sharepct=50.0)

    def test_get_latest_snapshot(self):
        """Test the get_latest_snapshot method returns most recent snapshot."""
        latest_snapshot = DeviceShare.objects.get_latest_snapshot()
        self.assertEqual(latest_snapshot.count(), 3)
        # Verify all returned items are from the latest snapshot
        for item in latest_snapshot:
            self.assertEqual(item.snapshotdate, 20251021)

    def test_get_latest_snapshot_ordered_by_device(self):
        """Test that get_latest_snapshot returns devices in alphabetical order."""
        latest_snapshot = list(DeviceShare.objects.get_latest_snapshot())
        devices = [item.device for item in latest_snapshot]
        self.assertEqual(devices, ["Desktop", "Mobile", "Tablet"])

    def test_get_latest_snapshot_empty(self):
        """Test get_latest_snapshot with no data."""
        DeviceShare.objects.all().delete()
        latest_snapshot = DeviceShare.objects.get_latest_snapshot()
        self.assertEqual(latest_snapshot.count(), 0)

    def test_snapshotdate_casting(self):
        """Test that the snapshotdate casting is applied in the queryset."""
        queryset = DeviceShare.objects.get_queryset()
        for obj in queryset:
            self.assertIsInstance(obj.snapshotdate_int, int)
            self.assertEqual(obj.snapshotdate_int, obj.snapshotdate)

    def test_multiple_snapshots(self):
        """Test querying across multiple snapshots."""
        all_devices = DeviceShare.objects.all()
        self.assertEqual(all_devices.count(), 6)

        # Verify we have 2 distinct snapshots
        snapshots = DeviceShare.objects.values_list('snapshotdate', flat=True).distinct()
        self.assertEqual(len(list(snapshots)), 2)

    def test_filter_by_device(self):
        """Test filtering by specific device across snapshots."""
        desktop_devices = DeviceShare.objects.filter(device="Desktop")
        self.assertEqual(desktop_devices.count(), 2)

        for device in desktop_devices:
            self.assertEqual(device.device, "Desktop")

    def test_sharepct_precision(self):
        """Test that share percentages maintain precision."""
        device = DeviceShare.objects.get(snapshotdate=20251020, device="Tablet")
        self.assertAlmostEqual(device.sharepct, 14.5, places=1)


class DeviceSharePointSerializerTest(BaseTestCase):
    """Tests for DeviceSharePointSerializer."""

    def test_field_mapping(self):
        """Test that device maps to name and sharepct maps to value."""
        device = DeviceShare.objects.create(
            snapshotdate=20251020,
            device="Desktop",
            sharepct=45.5
        )
        serializer = DeviceSharePointSerializer(device)

        self.assertIn("name", serializer.data)
        self.assertIn("value", serializer.data)
        self.assertEqual(serializer.data["name"], "Desktop")
        self.assertEqual(serializer.data["value"], 45.5)

    def test_excludes_original_fields(self):
        """Test that original field names are not in serialized output."""
        device = DeviceShare.objects.create(
            snapshotdate=20251020,
            device="Mobile",
            sharepct=40.0
        )
        serializer = DeviceSharePointSerializer(device)

        self.assertNotIn("device", serializer.data)
        self.assertNotIn("sharepct", serializer.data)
        self.assertNotIn("snapshotdate", serializer.data)

    def test_multiple_devices_serialization(self):
        """Test serializing multiple device objects."""
        devices = [
            DeviceShare.objects.create(snapshotdate=20251020, device="Desktop", sharepct=45.5),
            DeviceShare.objects.create(snapshotdate=20251020, device="Mobile", sharepct=40.0),
        ]
        serializer = DeviceSharePointSerializer(devices, many=True)

        self.assertEqual(len(serializer.data), 2)
        self.assertEqual(serializer.data[0]["name"], "Desktop")
        self.assertEqual(serializer.data[1]["name"], "Mobile")


class DeviceShareResponseSerializerTest(BaseTestCase):
    """Tests for DeviceShareResponseSerializer."""

    def setUp(self):
        """Create test data."""
        DeviceShare.objects.create(snapshotdate=20251020, device="Desktop", sharepct=45.5)
        DeviceShare.objects.create(snapshotdate=20251020, device="Mobile", sharepct=40.0)
        DeviceShare.objects.create(snapshotdate=20251020, device="Tablet", sharepct=14.5)
        DeviceShare.objects.create(snapshotdate=20251021, device="Desktop", sharepct=46.0)

    def test_field_mapping(self):
        """Maps device to name and sharepct to value."""
        devices = DeviceShare.objects.get_latest_snapshot()
        serializer = DeviceShareResponseSerializer({"data": devices})
        point = serializer.data["data"][0]

        self.assertIn("name", point)
        self.assertIn("value", point)
        self.assertNotIn("device", point)
        self.assertNotIn("sharepct", point)

    def test_response_structure(self):
        """Test that response has correct structure with 'data' key."""
        devices = DeviceShare.objects.get_latest_snapshot()
        serializer = DeviceShareResponseSerializer({"data": devices})

        self.assertIn("data", serializer.data)
        self.assertIsInstance(serializer.data["data"], list)

    def test_latest_snapshot_serialization(self):
        """Test serializing the latest snapshot returns correct count."""
        devices = DeviceShare.objects.get_latest_snapshot()
        serializer = DeviceShareResponseSerializer({"data": devices})

        # Should only return latest snapshot (20251021) which has 1 device
        self.assertEqual(len(serializer.data["data"]), 1)
        self.assertEqual(serializer.data["data"][0]["name"], "Desktop")
        self.assertEqual(serializer.data["data"][0]["value"], 46.0)

    def test_empty_data_serialization(self):
        """Test serializing empty queryset."""
        DeviceShare.objects.all().delete()
        devices = DeviceShare.objects.get_latest_snapshot()
        serializer = DeviceShareResponseSerializer({"data": devices})

        self.assertEqual(serializer.data["data"], [])

    def test_serializer_with_multiple_devices(self):
        """Test serialization with multiple devices in latest snapshot."""
        # Add more devices to the latest snapshot
        DeviceShare.objects.create(snapshotdate=20251021, device="Mobile", sharepct=39.5)
        DeviceShare.objects.create(snapshotdate=20251021, device="Tablet", sharepct=14.5)

        devices = DeviceShare.objects.get_latest_snapshot()
        serializer = DeviceShareResponseSerializer({"data": devices})

        self.assertEqual(len(serializer.data["data"]), 3)
        # Verify alphabetical ordering
        names = [point["name"] for point in serializer.data["data"]]
        self.assertEqual(names, ["Desktop", "Mobile", "Tablet"])

    def test_value_types(self):
        """Test that serialized values have correct types."""
        devices = DeviceShare.objects.get_latest_snapshot()
        serializer = DeviceShareResponseSerializer({"data": devices})

        for point in serializer.data["data"]:
            self.assertIsInstance(point["name"], str)
            self.assertIsInstance(point["value"], float)

    def test_share_percentage_precision(self):
        """Test that share percentages maintain precision through serialization."""
        DeviceShare.objects.create(snapshotdate=20251022, device="Desktop", sharepct=33.333)
        devices = DeviceShare.objects.get_latest_snapshot()
        serializer = DeviceShareResponseSerializer({"data": devices})

        self.assertAlmostEqual(serializer.data["data"][0]["value"], 33.333, places=3)
