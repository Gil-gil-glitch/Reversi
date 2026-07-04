import numpy as np

EMPTY = 0
BLACK = 1  # AnmitsuBot (Always player 1)
WHITE = 2  # Opponents (Always player 2)
SIZE = 8

DIRECTIONS = [(-1, -1), (-1, 0), (-1, 1), (0, 1), (1, 1), (1, 0), (1, -1), (0, -1)]

# Position matrix matching CastellaBot's Java configuration exactly
CASTELLA_MATRIX = np.array([
    [120, -20, 20, 5, 5, 20, -20, 120],
    [-20, -40, -5, -5, -5, -5, -40, -20],
    [20, -5, 15, 3, 3, 15, -5, 20],
    [5, -5, 3, 0, 0, 3, -5, 5],
    [5, -5, 3, 0, 0, 3, -5, 5],
    [20, -5, 15, 3, 3, 15, -5, 20],
    [-20, -40, -5, -5, -5, -5, -40, -20],
    [120, -20, 20, 5, 5, 20, -20, 120]
])

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

def get_valid_moves(board, player):
    moves = []
    for r in range(SIZE):
        for c in range(SIZE):
            if is_valid_move(board, r, c, player):
                moves.append((r, c))
    return moves

def make_move(board, row, col, player):
    opponent = WHITE if player == BLACK else BLACK
    board[row, col] = player
    for dr, dc in DIRECTIONS:
        r, c = row + dr, col + dc
        has_opponent = False
        while 0 <= r < SIZE and 0 <= c < SIZE and board[r, c] == opponent:
            has_opponent = True
            r += dr
            c += dc
        if has_opponent and 0 <= r < SIZE and 0 <= c < SIZE and board[r, c] == player:
            # Backtrack and flip pieces
            br, bc = r - dr, c - dc
            while br != row or bc != col:
                board[br, bc] = player
                br -= dr
                bc -= dc

# --- Baseline Opponent Bots ---
def get_dumb_move(board, player):
    moves = get_valid_moves(board, player)
    if not moves: return None
    return moves[np.random.choice(len(moves))]

def get_castella_move(board, player):
    moves = get_valid_moves(board, player)
    if not moves: return None

    best_score = -float('inf')
    best_move = moves[0]
    opponent = WHITE if player == BLACK else BLACK

    for r, c in moves:
        simulated = board.copy()
        make_move(simulated, r, c, player)

        # Lightweight static evaluation
        p_count = np.sum(simulated == player)
        o_count = np.sum(simulated == opponent)

        pos_score = np.sum(CASTELLA_MATRIX[simulated == player]) - np.sum(CASTELLA_MATRIX[simulated == opponent])
        score = (10 * (p_count - o_count)) + pos_score

        if score > best_score:
            best_score = score
            best_move = (r, c)

    return best_move