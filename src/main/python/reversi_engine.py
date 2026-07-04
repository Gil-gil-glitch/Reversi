import numpy as np

EMPTY = 0
BLACK = 1
WHITE = 2
SIZE = 8

DIRECTIONS = [(-1, -1), (-1, 0), (-1, 1), (0, 1), (1, 1), (1, 0), (1, -1), (0, -1)]

def initialize_board():
    board = np.zeros((SIZE, SIZE), dtype=int)
    board[3, 3] = WHITE
    board[3, 4] = BLACK
    board[4, 3] = BLACK
    board[4, 4] = WHITE
    return board

def is_valid_move(board, row, col, player):
    if board[row, col] != EMPTY:
        return False
    
    opponent = WHITE if player == BLACK else BLACK
    for dr, dc in DIRECTIONS:
        r, c = row + dr, col + dc
        found_opponent = False
        while 0 <= r < SIZE and 0 <= c < SIZE:
            if board[r, c] == opponent:
                found_opponent = True
            elif board[r, c] == player:
                if found_opponent:
                    return True
                break
            else:
                break
            r += dr
            c += dc
    return False