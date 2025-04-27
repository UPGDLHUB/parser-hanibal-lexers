import java.util.Vector;

public class TheParser {

    private Vector<TheToken> tokens;
    private int currentToken;

    public TheParser(Vector<TheToken> tokens) {
        this.tokens = tokens;
        currentToken = 0;
    }

    private boolean isType(String tokenValue) {
        return tokenValue.equals("int") ||
                tokenValue.equals("boolean") ||
                tokenValue.equals("float") ||
                tokenValue.equals("void") ||
                tokenValue.equals("char") ||
                tokenValue.equals("string");
    }

    public void run() {
        RULE_PROGRAM();
    }

    private void RULE_PROGRAM() {
        System.out.println("- RULE_PROGRAM");

        // Expect 'class'
        if (tokens.get(currentToken).getValue().equals("class")) {
            System.out.println("-- Found 'class'");
            currentToken++;
        } else {
            error(1);
        }

        // Expect an identifier (class name)
        if (tokens.get(currentToken).getType().equals("ID")) {
            System.out.println("-- Found class name: " + tokens.get(currentToken).getValue());
            currentToken++;
        } else {
            error(2);
        }

        // Expect '{'
        if (tokens.get(currentToken).getValue().equals("{")) {
            System.out.println("-- Found '{'");
            currentToken++;
        } else {
            error(3);
        }

        // Parse METHODS until '}'
        while (!tokens.get(currentToken).getValue().equals("}")) {
            RULE_METHODS();
        }
        // Expect '}'
        if (tokens.get(currentToken).getValue().equals("}")) {
            System.out.println("-- Found '}' to close program");
            currentToken++;
        } else {
            error(4);
        }
    }

    public void RULE_METHODS() {
        System.out.println("RULE_METHOD");

        // Parse the return type
        RULE_TYPE();

        // Expect an identifier (method name)
        if (tokens.get(currentToken).getType().equals("ID")) {
            System.out.println("-- Found method name: " + tokens.get(currentToken).getValue());
            currentToken++;
        } else {
            error(5);
        }

        // Expect '('
        if (tokens.get(currentToken).getValue().equals("(")) {
            System.out.println("-- Found '('");
            currentToken++;
        } else {
            error(6);
        }

        // Parse the parameter list
        RULE_PARAMS();

        // Expect ')'
        if (tokens.get(currentToken).getValue().equals(")")) {
            System.out.println("-- Found ')'");
            currentToken++;
        } else {
            error(7);
        }

        // Expect '{'
        if (tokens.get(currentToken).getValue().equals("{")) {
            System.out.println("-- Found '{'");
            currentToken++;
        } else {
            error(8);
        }

        // Parse the body
        while (!tokens.get(currentToken).getValue().equals("}")) {
            RULE_BODY();
        }

        // Expect '}'
        if (tokens.get(currentToken).getValue().equals("}")) {
            System.out.println("-- Found '}' to close method");
            currentToken++;
        } else {
            error(9);
        }
    }

    public void RULE_RETURN() {
        System.out.println("RULE_RETURN");

        //  Expect 'return'
        if (tokens.get(currentToken).getValue().equals("return")) {
            System.out.println("-- Found 'return'");
            currentToken++;
        } else {
            error(10);
        }

        // Optional expression
        String tokenValue = tokens.get(currentToken).getValue();
        if (!tokenValue.equals(";") && !tokenValue.equals("}")) {
            RULE_EXPRESSION();
        }
    }

    public void RULE_DO_WHILE() {
        System.out.println("RULE_DO_WHILE");
        // Expect 'do'
        if (tokens.get(currentToken).getValue().equals("do")) {
            System.out.println("-- Found 'do'");
            currentToken++;
        } else {
            error(11);
        }
        // Expect '{'
        if (tokens.get(currentToken).getValue().equals("{")) {
            System.out.println("-- Found '{' for do-while body");
            currentToken++;
            //  Parse body (statements inside the block)
            while (!tokens.get(currentToken).getValue().equals("}")) {
                RULE_BODY();
            }
            // Expect '}'
            if (tokens.get(currentToken).getValue().equals("}")) {
                System.out.println("-- Found '}'");
                currentToken++;
            } else {
                error(12);
            }
        } else {
            RULE_BODY();
        }
        //  Expect 'while'
        if (tokens.get(currentToken).getValue().equals("while")) {
            System.out.println("-- Found 'while' after do-body");
            currentToken++;
        } else {
            error(13);
        }
        //  Expect '('
        if (tokens.get(currentToken).getValue().equals("(")) {
            System.out.println("-- Found '(' in do-while");
            currentToken++;
        } else {
            error(14);
        }
        //  Parse expression
        RULE_EXPRESSION();
        //  Expect ')'
        if (tokens.get(currentToken).getValue().equals(")")) {
            System.out.println("-- Found ')' in do-while");
            currentToken++;
        } else {
            error(15);
        }
        //  Expect ';'
        if (tokens.get(currentToken).getValue().equals(";")) {
            System.out.println("-- Found ';' at end of do-while");
            currentToken++;
        } else {
            error(16);
        }
    }

    public void RULE_SWITCH() {
        System.out.println("RULE_SWITCH");
        // Expect 'switch'
        if (tokens.get(currentToken).getValue().equals("switch")) {
            System.out.println("-- Found 'switch'");
            currentToken++;
        } else {
            error(17);
        }
        // Expect '('
        if (tokens.get(currentToken).getValue().equals("(")) {
            System.out.println("-- Found '(' in switch");
            currentToken++;
        } else {
            error(18);
        }
        //  Parse expression
        RULE_EXPRESSION();
        //  Expect ')'
        if (tokens.get(currentToken).getValue().equals(")")) {
            System.out.println("-- Found ')' in switch");
            currentToken++;
        } else {
            error(19);
        }
        //  Expect '{'
        if (tokens.get(currentToken).getValue().equals("{")) {
            System.out.println("-- Found '{' to start switch body");
            currentToken++;
        } else {
            error(20);
        }
        // Parse one or more case blocks
        while (tokens.get(currentToken).getValue().equals("case")) {
            System.out.println("-- Found 'case'");
            currentToken++;
            //  Expect a constant (for simplicity, we check for a literal token type)
            String type = tokens.get(currentToken).getType();
            if (type.equals("INTEGER") || type.equals("OCTAL") || type.equals("HEXADECIMAL") ||
                    type.equals("BINARY") || type.equals("CHAR") || type.equals("STRING") || type.equals("ID")) {
                System.out.println("-- Found constant: " + tokens.get(currentToken).getValue());
                currentToken++;
            } else {
                error(21);
            }
            //. Expect ':'
            if (tokens.get(currentToken).getValue().equals(":")) {
                System.out.println("-- Found ':' after case constant");
                currentToken++;
            } else {
                error(22);
            }
            //  Parse the case body until next case/default or Last '}' reached
            while (!tokens.get(currentToken).getValue().equals("case") && !tokens.get(currentToken).getValue().equals("default") && !tokens.get(currentToken).getValue().equals("}")) {
                System.out.println(tokens.get(currentToken).getValue());
                RULE_BODY();
            }
//            // Optionally, if a 'break' is present, consume it along with its terminating ';'
//            if (tokens.get(currentToken).getValue().equals("break")) {
//                System.out.println("-- Found 'break' in case");
//                currentToken++;
//                if (tokens.get(currentToken).getValue().equals(";")) {
//                    System.out.println("-- Found ';' after break");
//                    currentToken++;
//                } else {
//                    error(23);
//                }
//            }
        }
        //  Optionally, parse a default block
        if (tokens.get(currentToken).getValue().equals("default")) {
            System.out.println("-- Found 'default'");
            currentToken++;
            if (tokens.get(currentToken).getValue().equals(":")) {
                System.out.println("-- Found ':' after default");
                currentToken++;
            } else {
                error(23);
            }
            while (!tokens.get(currentToken).getValue().equals("}")) {
                RULE_BODY();
            }
        }
        // Expect closing '}'
        if (tokens.get(currentToken).getValue().equals("}")) {
            System.out.println("-- Found '}' to close switch");
            currentToken++;
        } else {
            error(24);
        }
    }

    public void RULE_PRINT() {
        System.out.println("RULE_PRINT");

        if (tokens.get(currentToken).getValue().equals("print")) {
            System.out.println("-- Found 'print'");
            currentToken++;
        } else {
            error(24);
        }

        if (tokens.get(currentToken).getValue().equals("(")) {
            System.out.println("-- Found '(' in print");
            currentToken++;
        } else {
            error(26);
        }

        RULE_EXPRESSION();

        if (tokens.get(currentToken).getValue().equals(")")) {
            System.out.println("-- Found ')' in print");
            currentToken++;
        } else {
            error(27);
        }

        if (tokens.get(currentToken).getValue().equals(";")) {
            System.out.println("-- Found ';' at end of print");
            currentToken++;
        } else {
            error(28);
        }
    }

    public void RULE_BODY() {
        System.out.println("RULE_BODY");
        String tokenValue = tokens.get(currentToken).getValue();
        if (isType(tokenValue)) {
            RULE_VARIABLE();
            if (tokens.get(currentToken).getValue().equals(";")) {
                System.out.println("-- ;");
                currentToken++;
            } else {
                error(29);
            }
        } else if (tokenValue.equals("return")) {
            RULE_RETURN();
            if (tokens.get(currentToken).getValue().equals(";")) {
                System.out.println("-- ;");
                currentToken++;
            } else {
                error(30);
            }
        } else if (tokenValue.equals("break") || tokenValue.equals("continue")) {
            currentToken++;
            if (tokens.get(currentToken).getValue().equals(";")) {
                System.out.println("-- ;");
                currentToken++;
                return;
            } else {
                error(31);
            }
        } else if (tokenValue.equals("print")) {
            RULE_PRINT();
        } else if (tokenValue.equals("while")) {
            RULE_WHILE();
        } else if (tokenValue.equals("do")) {
            RULE_DO_WHILE();
        } else if (tokenValue.equals("for")) {
            RULE_FOR();
        } else if (tokenValue.equals("switch")) {
            RULE_SWITCH();
        } else if (tokenValue.equals("if")) {
            RULE_IF();
        } else if (tokens.get(currentToken).getType().equals("ID")) {
            String nextValue = tokens.get(currentToken + 1).getValue();
            if (nextValue.equals("(")) {
                RULE_CALL_METHOD();
            } else if (nextValue.equals("=")) {
                RULE_ASSIGNMENT();
            } else {
                error(32);
            }
            if (tokens.get(currentToken).getValue().equals(";")) {
                System.out.println("-- ;");
                currentToken++;
            } else {
                error(33);
            }
        } else if (tokenValue.equals(";")) {
            while (tokens.get(currentToken).getValue().equals(";")) {
                System.out.println("-- Alone ;");
                currentToken++;
            }
        } else {
            System.out.println(tokens.get(currentToken).getValue() + ": " + tokens.get(currentToken).getType());
            error(34);
        }
    }

//	public void RULE_BODY() {
//		System.out.println("-- RULE_BODY");
//		while (!tokens.get(currentToken).getValue().equals("}")) {
//			String tokenValue = tokens.get(currentToken).getValue();
//			if (tokenValue.equals("break") ||
//					tokenValue.equals("case") ||
//					tokenValue.equals("default")) {
//				return;
//			}
//			if (tokenValue.equals("while")) {
//				RULE_WHILE();
//			} else if (tokenValue.equals("do")) {
//				RULE_DO_WHILE();
//			} else if (tokenValue.equals("for")) {
//				RULE_FOR();
//			} else if (tokenValue.equals("switch")) {
//				RULE_SWITCH();
//			} else if (tokenValue.equals("if")) {
//				RULE_IF();
//			} else if (tokenValue.equals("return")) {
//				RULE_RETURN();
//				if (tokens.get(currentToken).getValue().equals(";")) {
//					System.out.println("-- ;");
//					currentToken++;
//				} else {
//					error(26);
//				}
//			} else if (isType(tokenValue)) {
//				RULE_VARIABLE();
//				if (tokens.get(currentToken).getValue().equals(";")) {
//					System.out.println("-- ;");
//					currentToken++;
//				} else {
//					error(27);
//				}
//			} else if (tokens.get(currentToken).getType().equals("ID")) {
//				String nextValue = tokens.get(currentToken + 1).getValue();
//				if (nextValue.equals("(")) {
//					RULE_CALL_METHOD();
//				} else if (nextValue.equals("=")) {
//					RULE_ASSIGNMENT();
//				} else {
//					error(28);
//				}
//				if (tokens.get(currentToken).getValue().equals(";")) {
//					System.out.println("-- ;");
//					currentToken++;
//				} else {
//					error(29);
//				}
//			} else {
//				error(30);
//			}
//		}
//		// Consume closing '}'
//	}

    public void RULE_ASSIGNMENT() {
        System.out.println("RULE_ASSIGNMENT");

        if (tokens.get(currentToken).getType().equals("ID")) {
            System.out.println("-- Found identifier for assignment: " + tokens.get(currentToken).getValue());
            currentToken++;
        } else {
            error(35);
        }

        if (tokens.get(currentToken).getValue().equals("=")) {
            System.out.println("-- Found '='");
            currentToken++;
        } else {
            error(36);
        }

        RULE_EXPRESSION();
    }

    public void RULE_CALL_METHOD() {
        System.out.println("RULE_CALL_METHOD");

        if (tokens.get(currentToken).getType().equals("ID")) {
            System.out.println("-- Found method name: " + tokens.get(currentToken).getValue());
            currentToken++;
        } else {
            error(37);
        }

        if (tokens.get(currentToken).getValue().equals("(")) {
            System.out.println("-- Found '('");
            currentToken++;
        } else {
            error(38);
        }

        RULE_PARAM_VALUES();

        if (tokens.get(currentToken).getValue().equals(")")) {
            System.out.println("-- Found ')'");
            currentToken++;
        } else {
            error(39);
        }
    }

    public void RULE_PARAM_VALUES() {
        System.out.println("RULE_PARAM_VALUES");

        if (tokens.get(currentToken).getValue().equals(")")) {
            System.out.println("-- No parameters");
            return;
        }

        RULE_EXPRESSION();

        while (tokens.get(currentToken).getValue().equals(",")) {
            System.out.println("-- Found ','");
            currentToken++;
            RULE_EXPRESSION();
        }
    }

    public void RULE_EXPRESSION() {
        System.out.println("--- RULE_EXPRESSION");
        RULE_X();
        while (tokens.get(currentToken).getValue().equals("|") ||
                tokens.get(currentToken).getValue().equals("||")) {
            currentToken++;
            System.out.println("--- |");
            RULE_X();
        }
    }

    public void RULE_X() {
        System.out.println("---- RULE_X");
        RULE_Y();
        while (tokens.get(currentToken).getValue().equals("&") ||
                tokens.get(currentToken).getValue().equals("&&")) {
            System.out.println("---- Found & or &&");
            currentToken++;
            RULE_Y();
        }
    }

    public void RULE_Y() {
        System.out.println("----- RULE_Y");
        while (tokens.get(currentToken).getValue().equals("!")) {
            System.out.println("----- !");
            currentToken++;
        }
        RULE_R();
    }

    public void RULE_R() {
        System.out.println("------ RULE_R");
        RULE_E();
        while (tokens.get(currentToken).getValue().equals("<") ||
                tokens.get(currentToken).getValue().equals(">") ||
                tokens.get(currentToken).getValue().equals("==") ||
                tokens.get(currentToken).getValue().equals("!=")) {
            currentToken++;
            System.out.println("------ relational operator");
            RULE_E();
        }
    }

    public void RULE_E() {
        System.out.println("------- RULE_E");
        RULE_A();
        while (tokens.get(currentToken).getValue().equals("-") ||
                tokens.get(currentToken).getValue().equals("+")) {
            currentToken++;
            System.out.println("------- + or -");
            RULE_A();
        }
    }

    public void RULE_A() {
        System.out.println("-------- RULE_A");
        RULE_B();
        while (tokens.get(currentToken).getValue().equals("/") ||
                tokens.get(currentToken).getValue().equals("*")) {
            currentToken++;
            System.out.println("-------- * or /");
            RULE_B();
        }
    }

    public void RULE_B() {
        System.out.println("--------- RULE_B");
        if (tokens.get(currentToken).getValue().equals("-")) {
            currentToken++;
            System.out.println("--------- -");
        }
        RULE_C();
    }

    public void RULE_C() {
        System.out.println("RULE_C");

        String tokenValue = tokens.get(currentToken).getValue();
        String tokenType = tokens.get(currentToken).getType();

        if (tokenType.equals("INTEGER")) {
            System.out.println("-- Found INTEGER: " + tokenValue);
            currentToken++;
        } else if (tokenType.equals("OCTAL")) {
            System.out.println("-- Found OCTAL: " + tokenValue);
            currentToken++;
        } else if (tokenType.equals("HEXADECIMAL")) {
            System.out.println("-- Found HEXADECIMAL: " + tokenValue);
            currentToken++;
        } else if (tokenType.equals("BINARY")) {
            System.out.println("-- Found BINARY: " + tokenValue);
            currentToken++;
        } else if (tokenValue.equals("true") || tokenValue.equals("false")) {
            System.out.println("-- Found BOOLEAN: " + tokenValue);
            currentToken++;
        } else if (tokenType.equals("STRING")) {
            System.out.println("-- Found STRING: " + tokenValue);
            currentToken++;
        } else if (tokenType.equals("CHAR")) {
            System.out.println("-- Found CHAR: " + tokenValue);
            currentToken++;
        } else if (tokenType.equals("FLOAT")) {
            System.out.println("-- Found FLOAT: " + tokenValue);
            currentToken++;
        } else if (tokenType.equals("ID")) {
            System.out.println("-- Found IDENTIFIER: " + tokenValue);
            currentToken++;
            if (tokens.get(currentToken).getValue().equals("(")) {
                System.out.println("-- Found '(' after identifier, starting function call");
                currentToken++;
                RULE_PARAM_VALUES();
                if (tokens.get(currentToken).getValue().equals(")")) {
                    System.out.println("-- Found ')' to close function call");
                    currentToken++;
                } else {
                    error(40);
                }
            }
        } else if (tokenValue.equals("(")) {
            System.out.println("-- Found '('");
            currentToken++;
            RULE_EXPRESSION();
            if (tokens.get(currentToken).getValue().equals(")")) {
                System.out.println("-- Found ')'");
                currentToken++;
            } else {
                error(41);
            }
        } else {
            error(42);
        }
    }

    public void RULE_TYPE() {
        System.out.println("RULE_TYPE");
        String tokenValue = tokens.get(currentToken).getValue();
        if (tokenValue.equals("int") ||
                tokenValue.equals("boolean") ||
                tokenValue.equals("float") ||
                tokenValue.equals("void") ||
                tokenValue.equals("char") ||
                tokenValue.equals("string")) {
            System.out.println("Type: " + tokenValue);
            currentToken++;
        }
    }

    public void RULE_PARAMS() {
        System.out.println("RULE_PARAMS");

        if (tokens.get(currentToken).getValue().equals(")")) {
            System.out.println("-- No parameters");
            return;
        }

        RULE_TYPE();
        if (tokens.get(currentToken).getType().equals("ID")) {
            System.out.println("Parameter ID: " + tokens.get(currentToken).getValue());
            currentToken++;
        } else {
            error(43);
        }

        while (tokens.get(currentToken).getValue().equals(",")) {
            System.out.println("Comma found in parameters");
            currentToken++;
            RULE_TYPE();
            if (tokens.get(currentToken).getType().equals("ID")) {
                System.out.println("Parameter ID: " + tokens.get(currentToken).getValue());
                currentToken++;
            } else {
                error(44);
            }
        }
    }

    // Modified RULE_FOR to allow a variable declaration in the initialization.
    public void RULE_FOR() {
        System.out.println("RULE_FOR");
        // Expect 'for'
        if (tokens.get(currentToken).getValue().equals("for")) {
            System.out.println("-- Found 'for'");
            currentToken++;
        } else {
            error(45);
        }
        // Expect '('
        if (tokens.get(currentToken).getValue().equals("(")) {
            System.out.println("-- Found '(' in for");
            currentToken++;
        } else {
            error(46);
        }
        // Optional initial expression or variable declaration:
        if (!tokens.get(currentToken).getValue().equals(";")) {
            // If it starts with a type keyword, parse a variable declaration.
            if (isType(tokens.get(currentToken).getValue())) {
                RULE_VARIABLE();
            } else {
                RULE_ASSIGNMENT();
            }
        }
        // Expect ';'
        if (tokens.get(currentToken).getValue().equals(";")) {
            System.out.println("-- Found first ';' in for");
            currentToken++;
        } else {
            error(47);
        }
        // Optional condition expression
        if (!tokens.get(currentToken).getValue().equals(";")) {
            RULE_EXPRESSION();
        }
        // Expect ';'
        if (tokens.get(currentToken).getValue().equals(";")) {
            System.out.println("-- Found second ';' in for");
            currentToken++;
        } else {
            error(48);
        }
        // Optional update expression
        if (!tokens.get(currentToken).getValue().equals(")")) {
            RULE_ASSIGNMENT();
        }
        // Expect ')'
        if (tokens.get(currentToken).getValue().equals(")")) {
            System.out.println("-- Found ')' in for");
            currentToken++;
        } else {
            error(49);
        }
        // Expect '{'
        if (tokens.get(currentToken).getValue().equals("{")) {
            System.out.println("-- Found '{' for for-body");
            currentToken++;
            //. Parse body statements
            while (!tokens.get(currentToken).getValue().equals("}")) {
                RULE_BODY();
            }
            //. Expect '}'
            if (tokens.get(currentToken).getValue().equals("}")) {
                System.out.println("-- Found '}' to close for-body");
                currentToken++;
            } else {
                error(50);
            }
        } else {
            RULE_BODY();
        }
    }

    public void RULE_WHILE() {
        System.out.println("RULE_WHILE");
        if (tokens.get(currentToken).getValue().equals("while")) {
            System.out.println("-- Found 'while'");
            currentToken++;
        } else {
            error(51);
        }
        if (tokens.get(currentToken).getValue().equals("(")) {
            System.out.println("-- Found '('");
            currentToken++;
        } else {
            error(52);
        }
        RULE_EXPRESSION();
        if (tokens.get(currentToken).getValue().equals(")")) {
            System.out.println("-- Found ')'");
            currentToken++;
        } else {
            error(53);
        }
        if (tokens.get(currentToken).getValue().equals("{")) {
            System.out.println("-- Found '{'");
            currentToken++;
            while (!tokens.get(currentToken).getValue().equals("}")) {
                RULE_BODY();
            }
            if (tokens.get(currentToken).getValue().equals("}")) {
                System.out.println("-- Found '}'");
                currentToken++;
            } else {
                error(54);
            }
        } else {
            RULE_BODY();
        }
    }

    public void RULE_VARIABLE() {
        System.out.println("RULE_VARIABLE");
        RULE_TYPE();
        if (tokens.get(currentToken).getType().equals("ID")) {
            System.out.println("VARIABLE identifier: " + tokens.get(currentToken).getValue());
            currentToken++;
        } else {
            error(55);
        }
        if (tokens.get(currentToken).getValue().equals("=")) {
            System.out.println("VARIABLE assignment operator: =");
            currentToken++;
            RULE_EXPRESSION();
        }
    }

    public void RULE_IF() {
        System.out.println("RULE_IF");
        if (tokens.get(currentToken).getValue().equals("if")) {
            System.out.println("-- Found 'if'");
            currentToken++;
        } else {
            error(56);
        }
        if (tokens.get(currentToken).getValue().equals("(")) {
            System.out.println("-- Found '('");
            currentToken++;
        } else {
            error(57);
        }
        RULE_EXPRESSION();
        if (tokens.get(currentToken).getValue().equals(")")) {
            System.out.println("-- Found ')'");
            currentToken++;
        } else {
            error(58);
        }
        if (tokens.get(currentToken).getValue().equals("{")) {
            System.out.println("-- Found '{' for IF body");
            currentToken++;
            while (!tokens.get(currentToken).getValue().equals("}")) {
                RULE_BODY();
            }
            if (tokens.get(currentToken).getValue().equals("}")) {
                System.out.println("-- Found '}' after IF body");
                currentToken++;
            } else {
                error(59);
            }
        } else {
            RULE_BODY();
        }

        if (tokens.get(currentToken).getValue().equals("else")) {
            currentToken++;

            if (tokens.get(currentToken).getValue().equals("if")) {
                System.out.println("-- Found 'else if'");
                RULE_IF();
                return;
            } else {
                System.out.println("-- Found 'else'");
                if (tokens.get(currentToken).getValue().equals("{")) {
                    System.out.println("-- Found '{' for ELSE body");
                    currentToken++;
                    while (!tokens.get(currentToken).getValue().equals("}")) {
                        RULE_BODY();
                    }
                    if (tokens.get(currentToken).getValue().equals("}")) {
                        System.out.println("-- Found '}' after IF body");
                        currentToken++;
                    } else {
                        error(60);
                    }
                } else {
                    RULE_BODY();
                }
            }
        }
    }

    private void error(int error) {
        ;
        System.out.println("Error " + error +
                " at token " + tokens.get(currentToken).toString() +
                " with the value " + tokens.get(currentToken).getValue());
        System.exit(1);
    }
}
