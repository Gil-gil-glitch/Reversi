import numpy as np

class AnmitsuBotPython:
    def __init__(self):
        self.num_features = 10
        self.alpha = 0.01
        
        # 10 features initialized to 0 for all 3 tactical game phases
        self.weights = {
            'OPENING': np.zeros(self.num_features),
            'MIDGAME': np.zeros(self.num_features),
            'ENDGAME': np.zeros(self.num_features)
        }

        # Coordinate definitions for specific tactical square types
        self.C_SQUARES = [(0, 1), (0, 6), (1, 0), (1, 7), (6, 0), (6, 7), (7, 1), (7, 6)]
        self.X_SQUARES = [(1, 1), (1, 6), (6, 1), (6, 6)]
        self.CORNERS = [(0, 0), (0, 7), (7, 0), (7, 7)]

    def get_game_phase(self, board):
        total_pieces = np.sum(board != 0)
        if total_pieces <= 20: return 'OPENING'
        if total_pieces <= 48: return 'MIDGAME'
        return 'ENDGAME'

    def extract_features(self, board, player):
        import reversi_engine as re
        
        features = np.zeros(self.num_features)
        opponent = 2 if player == 1 else 1
        
        # 1. Base Counts & Masks
        p_mask = (board == player)
        o_mask = (board == opponent)
        p_count = np.sum(p_mask)
        o_count = np.sum(o_mask)
        
        # F0: Piece Parity (Normalized)
        features[0] = (p_count - o_count) / 64.0
        
        # F1: Corner Occupancy
        p_corners = sum(1 for r, c in self.CORNERS if board[r, c] == player)
        o_corners = sum(1 for r, c in self.CORNERS if board[r, c] == opponent)
        features[1] = (p_corners - o_corners) / 4.0
        
        # F2: C-Squares Danger (Negative feature if stepped on blindly)
        p_csq = sum(1 for r, c in self.C_SQUARES if board[r, c] == player)
        o_csq = sum(1 for r, c in self.C_SQUARES if board[r, c] == opponent)
        features[2] = (p_csq - o_csq) / 8.0
        
        # F3: X-Squares Danger (Highly dangerous diagonals)
        p_xsq = sum(1 for r, c in self.X_SQUARES if board[r, c] == player)
        o_xsq = sum(1 for r, c in self.X_SQUARES if board[r, c] == opponent)
        features[3] = (p_xsq - o_xsq) / 4.0
        
        # F4: Actual Player Mobility (Number of valid options available)
        p_moves = len(re.get_valid_moves(board, player))
        features[4] = p_moves / 32.0  # Normalized against max practical choices
        
        # F5: Opponent Mobility (Restricting enemy choices is key)
        o_moves = len(re.get_valid_moves(board, opponent))
        features[5] = o_moves / 32.0
        
        # F6 & F7: Frontier Pieces (Pieces exposed to empty spaces)
        p_frontier = 0
        o_frontier = 0
        for r in range(8):
            for c in range(8):
                if board[r, c] != 0:
                    # Check if neighboring an empty slot
                    is_frontier = False
                    for dr, dc in re.DIRECTIONS:
                        nr, nc = r + dr, c + dc
                        if 0 <= nr < 8 and 0 <= nc < 8 and board[nr, nc] == 0:
                            is_frontier = True
                            break
                    if is_frontier:
                        if board[r, c] == player: p_frontier += 1
                        else: o_frontier += 1
                        
        features[6] = p_frontier / 64.0
        features[7] = o_frontier / 64.0
        
        # F8: Edge Stability (Pieces locked into outer edges)
        p_edges = np.sum(p_mask[0, :]) + np.sum(p_mask[7, :]) + np.sum(p_mask[:, 0]) + np.sum(p_mask[:, 7])
        o_edges = np.sum(o_mask[0, :]) + np.sum(o_mask[7, :]) + np.sum(o_mask[:, 0]) + np.sum(o_mask[:, 7])
        features[8] = (p_edges - o_edges) / 28.0
        
        # F9: Board Center Control (Occupying rows/cols 2-5)
        p_center = np.sum(p_mask[2:6, 2:6])
        o_center = np.sum(o_mask[2:6, 2:6])
        features[9] = (p_center - o_center) / 16.0

        return features

    def get_bot_move(self, board, player):
        import reversi_engine as re
        moves = re.get_valid_moves(board, player)
        if not moves: return None
        
        phase = self.get_game_phase(board)
        w = self.weights[phase]
        
        best_val = -float('inf')
        best_move = moves[0]
        
        for r, c in moves:
            simulated = board.copy()
            re.make_move(simulated, r, c, player)
            feats = self.extract_features(simulated, player)
            
            val = np.dot(feats, w)
            if val > best_val:
                best_val = val
                best_move = (r, c)
        return best_move

    def update_weights(self, history, final_score_diff):
        target_value = final_score_diff / 64.0
        
        for features, phase in reversed(history):
            w = self.weights[phase]
            predicted = np.dot(features, w)
            td_error = target_value - predicted
            
            # Updates all 10 features instantly using optimized vectorized broadcast
            self.weights[phase] += self.alpha * td_error * features
            target_value = predicted