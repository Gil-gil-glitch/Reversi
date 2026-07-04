import numpy as np

class AnmitsuBotPython:
    def __init__(self):
        self.num_features = 10
        self.alpha = 0.01
        
        # Fast weight layers mapped out as NumPy float vectors initialized to 0
        self.weights = {
            'OPENING': np.zeros(self.num_features),
            'MIDGAME': np.zeros(self.num_features),
            'ENDGAME': np.zeros(self.num_features)
        }

    def get_game_phase(self, board):
        total_pieces = np.sum(board != 0)
        if total_pieces <= 20: return 'OPENING'
        if total_pieces <= 48: return 'MIDGAME'
        return 'ENDGAME'

    def extract_features(self, board, player):
        features = np.zeros(self.num_features)
        opponent = 2 if player == 1 else 1
        
        p_count = np.sum(board == player)
        o_count = np.sum(board == opponent)
        
        # F0: Piece parity
        features[0] = (p_count - o_count) / 64.0
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
            
            # NumPy vector dot product replaces Java loops cleanly
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
            
            # Vectorized gradient update step
            self.weights[phase] += self.alpha * td_error * features
            target_value = predicted