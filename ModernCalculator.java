import java.awt.*;
import java.awt.event.*;
import java.util.Stack;
import javax.swing.*;

public class ModernCalculator extends JFrame implements ActionListener, KeyListener {

    private JTextField display;
    private boolean operatorPressed = false;
    private boolean isModulus = false;

    ModernCalculator() {
        setTitle("Modern Java Calculator");
        setSize(380, 550);
        setLayout(null);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(30, 30, 30));

        display = new JTextField();
        display.setBounds(20, 30, 330, 70);
        display.setFont(new Font("Arial", Font.BOLD, 28));
        display.setEditable(false);
        display.setBackground(new Color(50, 50, 50));
        display.setForeground(Color.WHITE);
        display.setHorizontalAlignment(SwingConstants.RIGHT);
        add(display);

        // Add key listener to capture keyboard input
        addKeyListener(this);
        setFocusable(true);  // Allow the frame to be focusable and capture key events

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(5, 4, 10, 10));
        buttonPanel.setBounds(20, 120, 330, 370);
        buttonPanel.setBackground(new Color(30, 30, 30));

        String buttons[] = {
            "C", "√", "%", "/",
            "7", "8", "9", "*",
            "4", "5", "6", "-",
            "1", "2", "3", "+",
            "0", ".", "=", "^"
        };

        for (String text : buttons) {
            JButton btn = createButton(text);
            buttonPanel.add(btn);
        }

        add(buttonPanel);
        setVisible(true);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);

        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setFocusable(false);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(60, 60, 60));
        button.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80), 2));
        button.setOpaque(true);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(90, 90, 90));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(60, 60, 60));
            }
        });

        button.addActionListener(this);
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        handleInput(command);
    }

    private void handleInput(String command) {
        try {
            // Numbers or decimal
            if ((command.charAt(0) >= '0' && command.charAt(0) <= '9') || command.equals(".")) {
                display.setText(display.getText() + command);
                operatorPressed = false;
            }

            // Clear
            else if (command.equals("C")) {
                display.setText("");
                operatorPressed = false;
                isModulus = false; 
            }

            // Square root
            else if (command.equals("√")) {
                double value = Double.parseDouble(display.getText());
                double result = Math.sqrt(value);
                display.setText(String.valueOf(result));
                operatorPressed = false;
            }

            // Percent (as a percentage calculation or modulus operator)
            else if (command.equals("%")) {
                if (operatorPressed) {
                    isModulus = true;
                    display.setText(display.getText() + " " + command + " ");
                } else {
                    double value = Double.parseDouble(display.getText());
                    double result = value / 100;
                    display.setText(String.valueOf(result));
                    operatorPressed = false;
                }
            }

            // Operator (+, -, *, /, ^)
            else if ("+-*/^".contains(command)) {
                String currentText = display.getText();
                if (operatorPressed) {
                    currentText = currentText.substring(0, currentText.length() - 2);
                }
                display.setText(currentText + " " + command + " ");
                operatorPressed = true;
                isModulus = false;
            }

            // Equal =
            else if (command.equals("=")) {
                String expression = display.getText().trim();

                if (expression.endsWith("+") || expression.endsWith("-") ||
                    expression.endsWith("*") || expression.endsWith("/") ||
                    expression.endsWith("^") || expression.endsWith("%")) {
                    expression = expression.substring(0, expression.length() - 2);
                }

                double result = evaluateExpression(expression);
                display.setText(String.valueOf(result));
                operatorPressed = false;
                isModulus = false;
            }

        } catch (Exception ex) {
            display.setText("Error");
        }
    }

    private double evaluateExpression(String expression) {
        String[] tokens = expression.split(" ");
        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();

        for (String token : tokens) {
            if (token.isEmpty()) continue;

            if (isNumeric(token)) {
                values.push(Double.parseDouble(token));
            } else if ("+-*/^%".contains(token)) {
                while (!operators.isEmpty() && hasPrecedence(token.charAt(0), operators.peek())) {
                    values.push(applyOperation(operators.pop(), values.pop(), values.pop()));
                }
                operators.push(token.charAt(0));
            }
        }

        while (!operators.isEmpty()) {
            values.push(applyOperation(operators.pop(), values.pop(), values.pop()));
        }

        return values.pop();
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private double applyOperation(char operator, double b, double a) {
        switch (operator) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/': 
                if (b == 0) {
                    display.setText("Error");
                    return 0;
                }
                return a / b;
            case '^': return Math.pow(a, b);
            case '%': return isModulus ? a % b : a / 100;
        }
        return 0;
    }

    private boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')') return false;
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-')) return false;
        if ((op1 == '^' || op1 == '%') && (op2 != '^' && op2 != '%')) return false;
        return true;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        char key = e.getKeyChar();
        String keyStr = String.valueOf(key);

        // Handle backspace
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            String currentText = display.getText();
            if (currentText.length() > 0) {
                display.setText(currentText.substring(0, currentText.length() - 1));
            }
        }
        // Handle enter (equals)
        else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            handleInput("=");
        }
        // Handle numbers and operators
        else if ("0123456789.+-*/^%".contains(keyStr)) {
            handleInput(keyStr);
        }
        // Handle clear (C key)
        else if (key == 'c' || key == 'C') {
            handleInput("C");
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        new ModernCalculator();
    }
}
