"""Safe deterministic arithmetic. This module never executes arbitrary Python."""
import ast
import math
import operator

_BINARY = {ast.Add: operator.add, ast.Sub: operator.sub, ast.Mult: operator.mul, ast.Div: operator.truediv, ast.FloorDiv: operator.floordiv, ast.Mod: operator.mod, ast.Pow: operator.pow}
_UNARY = {ast.UAdd: operator.pos, ast.USub: operator.neg}

class CalculationError(ValueError):
    pass

def _evaluate(node, depth=0):
    if depth > 24:
        raise CalculationError("The expression is too complex.")
    if isinstance(node, ast.Expression):
        return _evaluate(node.body, depth + 1)
    if isinstance(node, ast.Constant) and isinstance(node.value, (int, float)) and not isinstance(node.value, bool):
        value = node.value
    elif isinstance(node, ast.BinOp) and type(node.op) in _BINARY:
        left, right = _evaluate(node.left, depth + 1), _evaluate(node.right, depth + 1)
        if isinstance(node.op, ast.Pow) and abs(right) > 12:
            raise CalculationError("Powers above 12 are not supported yet.")
        try:
            value = _BINARY[type(node.op)](left, right)
        except ZeroDivisionError as exc:
            raise CalculationError("Division by zero is not allowed.") from exc
        except (OverflowError, ValueError) as exc:
            raise CalculationError("That calculation is outside the supported range.") from exc
    elif isinstance(node, ast.UnaryOp) and type(node.op) in _UNARY:
        value = _UNARY[type(node.op)](_evaluate(node.operand, depth + 1))
    else:
        raise CalculationError("Use numbers and arithmetic operators only: +, -, *, /, //, %, and parentheses.")
    if not isinstance(value, (int, float)) or not math.isfinite(value) or abs(value) > 1e100:
        raise CalculationError("The result is outside the supported numeric range.")
    return value

def calculate(expression):
    if not isinstance(expression, str) or not expression.strip():
        raise CalculationError("Please provide a calculation.")
    if len(expression) > 160:
        raise CalculationError("That expression is too long.")
    try:
        tree = ast.parse(expression.strip(), mode="eval")
    except SyntaxError as exc:
        raise CalculationError("I couldn't read that calculation. Check the numbers and operators.") from exc
    return _evaluate(tree)

def format_result(value):
    if isinstance(value, float) and value.is_integer():
        return str(int(value))
    return str(value)
