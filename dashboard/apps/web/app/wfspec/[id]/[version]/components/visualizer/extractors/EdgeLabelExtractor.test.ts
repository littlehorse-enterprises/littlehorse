import { faker } from '@faker-js/faker'
import { VariableType } from '../../../../../../../littlehorse-public-api/common_enums'
import { Comparator } from '../../../../../../../littlehorse-public-api/common_wfspec'
import { Edge } from '../../../../../../../littlehorse-public-api/wf_spec'
import EdgeLabelExtractor from './EdgeLabelExtractor'

describe('extracts edge label out of a LH Node', () => {

    it('label should be empty if the edge does not have a condition', () => {
        const edge: Edge = Edge.fromJSON({
            'sinkNodeName': '2-eating-donut-TASK'
        })

        const label = EdgeLabelExtractor.extract(edge)

        expect(label).toEqual('')
    })

    describe('when a variable is on the left side', () => {
        it.each([
            [ Comparator.GREATER_THAN, '>' ],
            [ Comparator.LESS_THAN, '<' ],
            [ Comparator.GREATER_THAN_EQ, '>=' ],
            [ Comparator.LESS_THAN_EQ, '<=' ],
            [ Comparator.EQUALS, '=' ],
            [ Comparator.NOT_EQUALS, '!=' ],
        ])('a NULL when literal value type is NULL', (comparator: Comparator, comparatorSymbol: string) => {
            const variableName = faker.lorem.word()

            const edge: Edge = Edge.fromJSON({
                'sinkNodeName': '2-eating-donut-TASK',
                'condition': {
                    comparator,
                    'left': {
                        variableName
                    },
                    'right': {
                        'literalValue': {
                            'type': VariableType.NULL
                        }
                    }
                }
            })

            const label = EdgeLabelExtractor.extract(edge)

            expect(label).toEqual(`${variableName} ${comparatorSymbol} NULL`)
        })

        it('a > b conditional when json path is provided and the literal value is of type NULL', () => {
            const jsonPath = `$.${faker.lorem.word()}`
            const variableName = faker.lorem.word()

            const edge: Edge = Edge.fromJSON({
                'sinkNodeName': '2-eating-donut-TASK',
                'condition': {
                    'comparator': Comparator.GREATER_THAN,
                    'left': {
                        jsonPath,
                        variableName
                    },
                    'right': {
                        'literalValue': {
                            'type': VariableType.NULL
                        }
                    }
                }
            })

            const label = EdgeLabelExtractor.extract(edge)

            expect(label).toEqual(`${jsonPath} within ${variableName} > NULL`)
        })

        it('a > b conditional when json path is provided', () => {
            const jsonPath = `$.${faker.lorem.word()}`
            const variableName = faker.lorem.word()
            const literalValue = Math.floor(Math.random() * 10)

            const edge: Edge = Edge.fromJSON({
                'sinkNodeName': '2-eating-donut-TASK',
                'condition': {
                    'comparator': Comparator.GREATER_THAN,
                    'left': {
                        jsonPath,
                        variableName
                    },
                    'right': {
                        'literalValue': {
                            'type': VariableType.INT,
                            'int': literalValue
                        }
                    }
                }
            })

            const label = EdgeLabelExtractor.extract(edge)

            expect(label).toEqual(`${jsonPath} within ${variableName} > ${literalValue}`)
        })

        it.each([
            [ Comparator.GREATER_THAN, '>', 'double', VariableType.DOUBLE, Math.random() * 10 ],
            [ Comparator.GREATER_THAN, '>', 'int', VariableType.INT, Math.floor(Math.random() * 10) ],
            [ Comparator.GREATER_THAN, '>', 'str', VariableType.STR, 'a word' ],
            [ Comparator.LESS_THAN, '<', 'double', VariableType.DOUBLE, Math.random() * 10 ],
            [ Comparator.LESS_THAN, '<', 'int', VariableType.INT, Math.floor(Math.random() * 10) ],
            [ Comparator.LESS_THAN, '<', 'str', VariableType.STR, 'a word' ],
            [ Comparator.GREATER_THAN_EQ, '>=', 'double', VariableType.DOUBLE, Math.random() * 10 ],
            [ Comparator.GREATER_THAN_EQ, '>=', 'int', VariableType.INT, Math.floor(Math.random() * 10) ],
            [ Comparator.GREATER_THAN_EQ, '>=', 'str', VariableType.STR, 'a word' ],
            [ Comparator.LESS_THAN_EQ, '<=', 'double', VariableType.DOUBLE, Math.random() * 10 ],
            [ Comparator.LESS_THAN_EQ, '<=', 'int', VariableType.INT, Math.floor(Math.random() * 10) ],
            [ Comparator.LESS_THAN_EQ, '<=', 'str', VariableType.STR, 'a word' ],
            [ Comparator.EQUALS, '=', 'double', VariableType.DOUBLE, Math.random() * 10 ],
            [ Comparator.EQUALS, '=', 'int', VariableType.INT, Math.floor(Math.random() * 10) ],
            [ Comparator.EQUALS, '=', 'str', VariableType.STR, 'a word' ],
            [ Comparator.EQUALS, '=', 'bool', VariableType.BOOL, true ],
            [ Comparator.EQUALS, '=', 'bool', VariableType.BOOL, false ],
            [ Comparator.NOT_EQUALS, '!=', 'double', VariableType.DOUBLE, Math.random() * 10 ],
            [ Comparator.NOT_EQUALS, '!=', 'int', VariableType.INT, Math.floor(Math.random() * 10) ],
            [ Comparator.NOT_EQUALS, '!=', 'str', VariableType.STR, 'a word' ],
            [ Comparator.NOT_EQUALS, '!=', 'bool', VariableType.BOOL, true ],
            [ Comparator.NOT_EQUALS, '!=', 'bool', VariableType.BOOL, false ],
        ])('a %s b with type: %s and value: %s', (comparator: Comparator, comparatorSymbol: string, literalValueType: string, variableType: VariableType, variableValue: any) => {
            const variableName = faker.lorem.word()

            const edge: Edge = Edge.fromJSON({
                'sinkNodeName': '2-eating-donut-TASK',
                'condition': {
                    comparator,
                    'left': {
                        variableName
                    },
                    'right': {
                        'literalValue': {
                            'type': variableType,
                            [literalValueType]: variableValue
                        }
                    }
                }
            })

            const label = EdgeLabelExtractor.extract(edge)

            expect(label).toEqual(`${variableName} ${comparatorSymbol} ${variableValue}`)
        })

        it.each([
            [ [ 1.1, 2.1, 3.3 ] ],
            [ [ 1, 2, 3 ] ],
            [ [ '1', '2', '3' ] ]
        ])('a IN b with for array: %s', (variableValue: []) => {
            const variableName = faker.lorem.word()

            const edge: Edge = Edge.fromJSON({
                'sinkNodeName': '2-eating-donut-TASK',
                'condition': {
                    'comparator': Comparator.IN,
                    'left': {
                        variableName
                    },
                    'right': {
                        'literalValue': {
                            'type': 'JSON_ARR',
                            'jsonArr': `[${variableValue}]`
                        }
                    }
                }
            })

            const label = EdgeLabelExtractor.extract(edge)

            expect(label).toEqual(`${variableName} IN [${variableValue}]`)
        })

        it.each([
            [ [ 1.1, 2.1, 3.3 ] ],
            [ [ 1, 2, 3 ] ],
            [ [ '1', '2', '3' ] ]
        ])('a NOT IN b with for array: %s', (variableValue: []) => {
            const variableName = faker.lorem.word()

            const edge: Edge = Edge.fromJSON({
                'sinkNodeName': '2-eating-donut-TASK',
                'condition': {
                    'comparator': Comparator.NOT_IN,
                    'left': {
                        variableName
                    },
                    'right': {
                        'literalValue': {
                            'type': 'JSON_ARR',
                            'jsonArr': `[${variableValue}]`
                        }
                    }
                }
            })

            const label = EdgeLabelExtractor.extract(edge)

            expect(label).toEqual(`${variableName} NOT IN [${variableValue}]`)
        })
    })

    describe('when a variable is on the right side', () => {
        it.each([
            [ Comparator.GREATER_THAN, '>' ],
            [ Comparator.LESS_THAN, '<' ],
            [ Comparator.GREATER_THAN_EQ, '>=' ],
            [ Comparator.LESS_THAN_EQ, '<=' ],
            [ Comparator.EQUALS, '=' ],
            [ Comparator.NOT_EQUALS, '!=' ],
        ])('a %s NULL when literal value type is NULL', (comparator: Comparator, comparatorSymbol: string) => {
            const variableName = faker.lorem.word()

            const edge: Edge = Edge.fromJSON({
                'sinkNodeName': '2-eating-donut-TASK',
                'condition': {
                    comparator,
                    'left': {
                        'literalValue': {
                            'type': VariableType.NULL
                        }
                    },
                    'right': {
                        variableName
                    }
                }
            })

            const label = EdgeLabelExtractor.extract(edge)

            expect(label).toEqual(`NULL ${comparatorSymbol} ${variableName}`)
        })

        it('a > b conditional when json path is provided and the literal value is of type NULL', () => {
            const jsonPath = `$.${faker.lorem.word()}`
            const variableName = faker.lorem.word()

            const edge: Edge = Edge.fromJSON({
                'sinkNodeName': '2-eating-donut-TASK',
                'condition': {
                    'comparator': Comparator.GREATER_THAN,
                    'left': {
                        'literalValue': {
                            'type': VariableType.NULL
                        }
                    },
                    'right': {
                        jsonPath,
                        variableName
                    }
                }
            })

            const label = EdgeLabelExtractor.extract(edge)

            expect(label).toEqual(`NULL > ${jsonPath} within ${variableName}`)
        })

        it.each([
            [ Comparator.GREATER_THAN, '>', 'double', VariableType.DOUBLE, Math.random() * 10 ],
            [ Comparator.GREATER_THAN, '>', 'int', VariableType.INT, Math.floor(Math.random() * 10) ],
            [ Comparator.GREATER_THAN, '>', 'str', VariableType.STR, 'a word' ],
            [ Comparator.LESS_THAN, '<', 'double', VariableType.DOUBLE, Math.random() * 10 ],
            [ Comparator.LESS_THAN, '<', 'int', VariableType.INT, Math.floor(Math.random() * 10) ],
            [ Comparator.LESS_THAN, '<', 'str', VariableType.STR, 'a word' ],
            [ Comparator.GREATER_THAN_EQ, '>=', 'double', VariableType.DOUBLE, Math.random() * 10 ],
            [ Comparator.GREATER_THAN_EQ, '>=', 'int', VariableType.INT, Math.floor(Math.random() * 10) ],
            [ Comparator.GREATER_THAN_EQ, '>=', 'str', VariableType.STR, 'a word' ],
            [ Comparator.LESS_THAN_EQ, '<=', 'double', VariableType.DOUBLE, Math.random() * 10 ],
            [ Comparator.LESS_THAN_EQ, '<=', 'int', VariableType.INT, Math.floor(Math.random() * 10) ],
            [ Comparator.LESS_THAN_EQ, '<=', 'str', VariableType.STR, 'a word' ],
            [ Comparator.EQUALS, '=', 'double', VariableType.DOUBLE, Math.random() * 10 ],
            [ Comparator.EQUALS, '=', 'int', VariableType.INT, Math.floor(Math.random() * 10) ],
            [ Comparator.EQUALS, '=', 'str', VariableType.STR, 'a word' ],
            [ Comparator.NOT_EQUALS, '!=', 'double', VariableType.DOUBLE, Math.random() * 10 ],
            [ Comparator.NOT_EQUALS, '!=', 'int', VariableType.INT, Math.floor(Math.random() * 10) ],
            [ Comparator.NOT_EQUALS, '!=', 'str', VariableType.STR, 'a word' ],
        ])('a %s b with type: %s and value: %s', (comparator: Comparator, comparatorSymbol: string, literalValueType: string, variableType: VariableType, variableValue: any) => {
            const variableName = faker.lorem.word()

            const edge: Edge = Edge.fromJSON({
                'sinkNodeName': '2-eating-donut-TASK',
                'condition': {
                    comparator,
                    'left': {
                        'literalValue': {
                            'type': variableType,
                            [literalValueType]: variableValue
                        }

                    },
                    'right': {
                        variableName
                    }
                }
            })

            const label = EdgeLabelExtractor.extract(edge)

            expect(label).toEqual(`${variableValue} ${comparatorSymbol} ${variableName}`)
        })


        it('a > b conditional when json path is provided', () => {
            const jsonPath = `$.${faker.lorem.word()}`
            const variableName = faker.lorem.word()
            const literalValue = Math.floor(Math.random() * 10)

            const edge: Edge = Edge.fromJSON({
                'sinkNodeName': '2-eating-donut-TASK',
                'condition': {
                    'comparator': Comparator.GREATER_THAN,
                    'left': {
                        'literalValue': {
                            'type': VariableType.INT,
                            'int': literalValue
                        }
                    },
                    'right': {
                        jsonPath,
                        variableName
                    }
                }
            })

            const label = EdgeLabelExtractor.extract(edge)

            expect(label).toEqual(`${literalValue} > ${jsonPath} within ${variableName}`)
        })

        it.each([
            [ [ 1.1, 2.1, 3.3 ] ],
            [ [ 1, 2, 3 ] ],
            [ [ '1', '2', '3' ] ]
        ])('a IN b with for array: %s', (variableValue: []) => {
            const variableName = faker.lorem.word()

            const edge: Edge = Edge.fromJSON({
                'sinkNodeName': '3-task-b-TASK',
                'condition': {
                    'comparator': Comparator.IN,
                    'left': {
                        'literalValue': {
                            'type': 'JSON_ARR',
                            'jsonArr': `[${variableValue}]`
                        }
                    },
                    'right': {
                        variableName
                    }
                }
            })

            const label = EdgeLabelExtractor.extract(edge)

            expect(label).toEqual(`[${variableValue}] IN ${variableName}`)
        })

        it.each([
            [ [ 1.1, 2.1, 3.3 ] ],
            [ [ 1, 2, 3 ] ],
            [ [ '1', '2', '3' ] ]
        ])('a NOT IN b with for array: %s', (variableValue: []) => {
            const variableName = faker.lorem.word()

            const edge: Edge = Edge.fromJSON({
                'sinkNodeName': '3-task-b-TASK',
                'condition': {
                    'comparator': Comparator.IN,
                    'left': {
                        'literalValue': {
                            'type': 'JSON_ARR',
                            'jsonArr': `[${variableValue}]`
                        }
                    },
                    'right': {
                        variableName
                    }
                }
            })

            const label = EdgeLabelExtractor.extract(edge)

            expect(label).toEqual(`[${variableValue}] IN ${variableName}`)
        })
    })

    describe('when a variable is on both sides', () => {

        it.each([
            [ Comparator.GREATER_THAN, '>' ],
            [ Comparator.GREATER_THAN_EQ, '>=' ],
            [ Comparator.LESS_THAN, '<' ],
            [ Comparator.LESS_THAN_EQ, '<=' ],
            [ Comparator.EQUALS, '=' ],
            [ Comparator.NOT_EQUALS, '!=' ],
            [ Comparator.IN, 'IN' ],
            [ Comparator.NOT_IN, 'NOT IN' ]
        ])('a %s conditional', (comparator: Comparator, comparatorSymbol: string) => {
            const varOnTheLeft = faker.lorem.word()
            const varOnTheRight = faker.lorem.word()

            const edge: Edge = Edge.fromJSON({
                'sinkNodeName': '2-eating-donut-TASK',
                'condition': {
                    comparator,
                    'left': {
                        'variableName': varOnTheLeft
                    },
                    'right': {
                        'variableName': varOnTheRight
                    }
                }
            })

            const label = EdgeLabelExtractor.extract(edge)

            expect(label).toEqual(`${varOnTheLeft} ${comparatorSymbol} ${varOnTheRight}`)
        })

        it('a > b conditional when json path is provided', () => {
            const leftJsonPath = `$.${faker.lorem.word()}`
            const leftVariableName = faker.lorem.word()
            const rightJsonPath = `$.${faker.lorem.word()}`
            const rightVariableName = faker.lorem.word()


            const edge: Edge = Edge.fromJSON({
                'sinkNodeName': '2-eating-donut-TASK',
                'condition': {
                    'comparator': Comparator.GREATER_THAN,
                    'left': {
                        'jsonPath': leftJsonPath,
                        'variableName': leftVariableName
                    },
                    'right': {
                        'jsonPath': rightJsonPath,
                        'variableName': rightVariableName
                    }
                }
            })

            const label = EdgeLabelExtractor.extract(edge)

            expect(label).toEqual(`${leftJsonPath} within ${leftVariableName} > ${rightJsonPath} within ${rightVariableName}`)
        })
    })

    describe('when a literal is on both sides', () => {
        it.each([
            [ 'double', VariableType.DOUBLE, 2.1, 1.1 ],
            [ 'int', VariableType.INT, 2, 1 ],
            [ 'str', VariableType.STR, 'a', 'b' ]
        ])('a > b with type: %s and value: %s', (literalValueType: string, variableType: VariableType, leftVariableValue: any, rightVariableValue: any) => {
            const edge: Edge = Edge.fromJSON({
                'sinkNodeName': '2-eating-donut-TASK',
                'condition': {
                    'comparator': Comparator.GREATER_THAN,
                    'left': {
                        'literalValue': {
                            'type': variableType,
                            [literalValueType]: leftVariableValue
                        }
                    },
                    'right': {
                        'literalValue': {
                            'type': variableType,
                            [literalValueType]: rightVariableValue
                        }
                    }
                }
            })

            const label = EdgeLabelExtractor.extract(edge)

            expect(label).toEqual(`${leftVariableValue} > ${rightVariableValue}`)
        })

        it.each([
            [ 'double', VariableType.DOUBLE, 1.1, 2.1 ],
            [ 'int', VariableType.INT, 1, 2 ],
            [ 'str', VariableType.STR, 'b', 'a' ]
        ])('a < b with type: %s and value: %s', (literalValueType: string, variableType: VariableType, leftVariableValue: any, rightVariableValue: any) => {
            const edge: Edge = Edge.fromJSON({
                'sinkNodeName': '2-eating-donut-TASK',
                'condition': {
                    'comparator': Comparator.LESS_THAN,
                    'left': {
                        'literalValue': {
                            'type': variableType,
                            [literalValueType]: leftVariableValue
                        }
                    },
                    'right': {
                        'literalValue': {
                            'type': variableType,
                            [literalValueType]: rightVariableValue
                        }
                    }
                }
            })

            const label = EdgeLabelExtractor.extract(edge)

            expect(label).toEqual(`${leftVariableValue} < ${rightVariableValue}`)
        })

        it.each([
            [ 'double', VariableType.DOUBLE, 1.1, 2.1 ],
            [ 'int', VariableType.INT, 1, 1 ],
            [ 'str', VariableType.STR, 'b', 'a' ]
        ])('a <= b with type: %s and value: %s', (literalValueType: string, variableType: VariableType, leftVariableValue: any, rightVariableValue: any) => {
            const edge: Edge = Edge.fromJSON({
                'sinkNodeName': '2-eating-donut-TASK',
                'condition': {
                    'comparator': Comparator.LESS_THAN_EQ,
                    'left': {
                        'literalValue': {
                            'type': variableType,
                            [literalValueType]: leftVariableValue
                        }
                    },
                    'right': {
                        'literalValue': {
                            'type': variableType,
                            [literalValueType]: rightVariableValue
                        }
                    }
                }
            })

            const label = EdgeLabelExtractor.extract(edge)

            expect(label).toEqual(`${leftVariableValue} <= ${rightVariableValue}`)
        })

        it.each([
            [ 'double', VariableType.DOUBLE, 2.1, 1.1 ],
            [ 'int', VariableType.INT, 2, 1 ],
            [ 'str', VariableType.STR, 'a', 'b' ]
        ])('a >= b with type: %s and value: %s', (literalValueType: string, variableType: VariableType, leftVariableValue: any, rightVariableValue: any) => {
            const edge: Edge = Edge.fromJSON({
                'sinkNodeName': '2-eating-donut-TASK',
                'condition': {
                    'comparator': Comparator.GREATER_THAN_EQ,
                    'left': {
                        'literalValue': {
                            'type': variableType,
                            [literalValueType]: leftVariableValue
                        }
                    },
                    'right': {
                        'literalValue': {
                            'type': variableType,
                            [literalValueType]: rightVariableValue
                        }
                    }
                }
            })

            const label = EdgeLabelExtractor.extract(edge)

            expect(label).toEqual(`${leftVariableValue} >= ${rightVariableValue}`)
        })


        it.each([
            [ 'double', VariableType.DOUBLE, 1.1, 1.1 ],
            [ 'int', VariableType.INT, 1, 1 ],
            [ 'str', VariableType.STR, 'a', 'a' ]
        ])('a = b with type: %s and value: %s', (literalValueType: string, variableType: VariableType, leftVariableValue: any, rightVariableValue: any) => {
            const edge: Edge = Edge.fromJSON({
                'sinkNodeName': '2-eating-donut-TASK',
                'condition': {
                    'comparator': Comparator.EQUALS,
                    'left': {
                        'literalValue': {
                            'type': variableType,
                            [literalValueType]: leftVariableValue
                        }
                    },
                    'right': {
                        'literalValue': {
                            'type': variableType,
                            [literalValueType]: rightVariableValue
                        }
                    }
                }
            })

            const label = EdgeLabelExtractor.extract(edge)

            expect(label).toEqual(`${leftVariableValue} = ${rightVariableValue}`)
        })

        it.each([
            [ 'double', VariableType.DOUBLE, 2.1, 1.1 ],
            [ 'int', VariableType.INT, 2, 1 ],
            [ 'str', VariableType.STR, 'a', 'b' ]
        ])('a != b with type: %s and value: %s', (literalValueType: string, variableType: VariableType, leftVariableValue: any, rightVariableValue: any) => {
            const edge: Edge = Edge.fromJSON({
                'sinkNodeName': '2-eating-donut-TASK',
                'condition': {
                    'comparator': Comparator.NOT_EQUALS,
                    'left': {
                        'literalValue': {
                            'type': variableType,
                            [literalValueType]: leftVariableValue
                        }
                    },
                    'right': {
                        'literalValue': {
                            'type': variableType,
                            [literalValueType]: rightVariableValue
                        }
                    }
                }
            })

            const label = EdgeLabelExtractor.extract(edge)

            expect(label).toEqual(`${leftVariableValue} != ${rightVariableValue}`)
        })

        it.each([
            [ VariableType.INT, VariableType.JSON_ARR, 'int', 'jsonArr', 1.1, '[1.1, 2.1, 3.3]' ],
            [ VariableType.INT, VariableType.JSON_ARR, 'int', 'jsonArr', 1, '[1, 2, 3]' ],
            [ VariableType.INT, VariableType.JSON_ARR, 'str', 'jsonArr', '1', '["1", "2", "3"]' ],
            [ VariableType.STR, VariableType.STR, 'str', 'str', '1', '1234' ],
            [ VariableType.INT, VariableType.STR, 'int', 'str', 1, '1234' ],
            [ VariableType.DOUBLE, VariableType.STR, 'double', 'str', 1.0, '1.0234' ],
            [ VariableType.JSON_OBJ, VariableType.JSON_OBJ, 'jsonObj', 'jsonObj', '{"a":1}', '{"a":1}' ],
            [ VariableType.STR, VariableType.JSON_OBJ, 'str', 'jsonObj', 'a', '{"a":1}' ],
            [ VariableType.JSON_ARR, VariableType.JSON_ARR, 'jsonArr', 'jsonArr', '[1,2]', '[1,2]' ],
        ])('a IN b with for array: %s', (leftLiteralValueType: VariableType, rightLiteralValueType: VariableType, leftLiteralValueKey: string, rightLiteralValueKey: string, leftLiteralValue: any, rightLiteralValue: string) => {

            const edge: Edge = Edge.fromJSON({
                'sinkNodeName': '2-eating-donut-TASK',
                'condition': {
                    'comparator': Comparator.IN,
                    'left': {
                        'literalValue': {
                            'type': leftLiteralValueType,
                            [leftLiteralValueKey]: leftLiteralValue
                        }
                    },
                    'right': {
                        'literalValue': {
                            'type': rightLiteralValueType,
                            [rightLiteralValueKey]: rightLiteralValue
                        }
                    }
                }
            })

            const label = EdgeLabelExtractor.extract(edge)

            expect(label).toEqual(`${leftLiteralValue} IN ${rightLiteralValue}`)
        })

        it.each([
            [ VariableType.INT, VariableType.JSON_ARR, 'int', 'jsonArr', 1.1, '[1.1, 2.1, 3.3]' ],
            [ VariableType.INT, VariableType.JSON_ARR, 'int', 'jsonArr', 1, '[1, 2, 3]' ],
            [ VariableType.INT, VariableType.JSON_ARR, 'str', 'jsonArr', '1', '["1", "2", "3"]' ],
            [ VariableType.STR, VariableType.STR, 'str', 'str', '1', '1234' ],
            [ VariableType.INT, VariableType.STR, 'int', 'str', 1, '1234' ],
            [ VariableType.DOUBLE, VariableType.STR, 'double', 'str', 1.0, '1.0234' ],
            [ VariableType.JSON_OBJ, VariableType.JSON_OBJ, 'jsonObj', 'jsonObj', '{"a":1}', '{"a":1}' ],
            [ VariableType.STR, VariableType.JSON_OBJ, 'str', 'jsonObj', 'a', '{"a":1}' ],
            [ VariableType.JSON_ARR, VariableType.JSON_ARR, 'jsonArr', 'jsonArr', '[1,2]', '[1,2]' ],
        ])('a NOT IN b with for array: %s', (leftLiteralValueType: VariableType, rightLiteralValueType: VariableType, leftLiteralValueKey: string, rightLiteralValueKey: string, leftLiteralValue: any, rightLiteralValue: string) => {

            const edge: Edge = Edge.fromJSON({
                'sinkNodeName': '2-eating-donut-TASK',
                'condition': {
                    'comparator': Comparator.NOT_IN,
                    'left': {
                        'literalValue': {
                            'type': leftLiteralValueType,
                            [leftLiteralValueKey]: leftLiteralValue
                        }
                    },
                    'right': {
                        'literalValue': {
                            'type': rightLiteralValueType,
                            [rightLiteralValueKey]: rightLiteralValue
                        }
                    }
                }
            })

            const label = EdgeLabelExtractor.extract(edge)

            expect(label).toEqual(`${leftLiteralValue} NOT IN ${rightLiteralValue}`)
        })


    })
})
