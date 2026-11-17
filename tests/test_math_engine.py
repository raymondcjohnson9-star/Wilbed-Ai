import unittest
from atlas_core.math_engine import CalculationError, calculate, format_result

class MathEngineTests(unittest.TestCase):
    def test_basic_operations(self):
        self.assertEqual(calculate("2 + 3 * 4"), 14)
        self.assertEqual(calculate("(10 - 4) / 2"), 3)
        self.assertEqual(calculate("2 ** 3"), 8)
    def test_integer_float_format(self):
        self.assertEqual(format_result(5.0), "5")
    def test_rejects_python_execution(self):
        with self.assertRaises(CalculationError):
            calculate("__import__('os').system('echo unsafe')")
    def test_rejects_division_by_zero(self):
        with self.assertRaises(CalculationError):
            calculate("5 / 0")
    def test_rejects_large_exponent(self):
        with self.assertRaises(CalculationError):
            calculate("2 ** 100")

if __name__ == "__main__":
    unittest.main()
