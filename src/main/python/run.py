import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import reversi_engine as re

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
        features = np.zeros(self.num_features)
        opponent = 2 if player == 1 else 1
        
        p_mask = (board == player)
        o_mask = (board == opponent)
        p_count = np.sum(p_mask)
        o_count = np.sum(o_mask)
        
        # F0: Piece Parity
        features[0] = (p_count - o_count) / 64.0
        
        # F1: Corner Occupancy
        p_corners = sum(1 for r, c in self.CORNERS if board[r, c] == player)
        o_corners = sum(1 for r, c in self.CORNERS if board[r, c] == opponent)
        features[1] = (p_corners - o_corners) / 4.0
        
        # F2: C-Squares Danger
        p_csq = sum(1 for r, c in self.C_SQUARES if board[r, c] == player)
        o_csq = sum(1 for r, c in self.C_SQUARES if board[r, c] == opponent)
        features[2] = (p_csq - o_csq) / 8.0
        
        # F3: X-Squares Danger
        p_xsq = sum(1 for r, c in self.X_SQUARES if board[r, c] == player)
        o_xsq = sum(1 for r, c in self.X_SQUARES if board[r, c] == opponent)
        features[3] = (p_xsq - o_xsq) / 4.0
        
        # F4: Player Mobility
        p_moves = len(re.get_valid_moves(board, player))
        features[4] = p_moves / 32.0
        
        # F5: Opponent Mobility
        o_moves = len(re.get_valid_moves(board, opponent))
        features[5] = o_moves / 32.0
        
        # F6 & F7: Frontier Pieces
        p_frontier = 0
        o_frontier = 0
        for r in range(8):
            for c in range(8):
                if board[r, c] != 0:
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
        
        # F8: Edge Stability
        p_edges = np.sum(p_mask[0, :]) + np.sum(p_mask[7, :]) + np.sum(p_mask[:, 0]) + np.sum(p_mask[:, 7])
        o_edges = np.sum(o_mask[0, :]) + np.sum(o_mask[7, :]) + np.sum(o_mask[:, 0]) + np.sum(o_mask[:, 7])
        features[8] = (p_edges - o_edges) / 28.0
        
        # F9: Center Control
        p_center = np.sum(p_mask[2:6, 2:6])
        o_center = np.sum(o_mask[2:6, 2:6])
        features[9] = (p_center - o_center) / 16.0

        return features

    def get_bot_move(self, board, player):
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
            
            self.weights[phase] += self.alpha * td_error * features
            target_value = predicted

def play_match(agent, opponent_type):
    board = re.initialize_board()
    player1, player2 = 1, 2
    current_player = player1
    history = []
    
    while len(re.get_valid_moves(board, player1)) > 0 or len(re.get_valid_moves(board, player2)) > 0:
        moves = re.get_valid_moves(board, current_player)
        if moves:
            if current_player == player1:
                phase = agent.get_game_phase(board)
                feats = agent.extract_features(board, player1)
                history.append((feats, phase))
                
                if np.random.rand() < 0.1:  # 10% Exploration
                    move = moves[np.random.choice(len(moves))]
                else:
                    move = agent.get_bot_move(board, player1)
            else:
                if opponent_type == 'DumbBot':
                    move = re.get_dumb_move(board, player2)
                else:
                    move = re.get_castella_move(board, player2)
                    
            if move:
                re.make_move(board, move[0], move[1], current_player)
        current_player = 2 if current_player == 1 else 1
        
    p1_final = np.sum(board == player1)
    p2_final = np.sum(board == player2)
    
    agent.update_weights(history, p1_final - p2_final)
    return p1_final > p2_final

if __name__ == '__main__':
    print("Launching Vectorized Python Training Engine...")
    agent = AnmitsuBotPython()
    log_data = []

    epochs_per_stage = 1500
    window_size = 5
    epoch_counter = 0

    for opponent in ['DumbBot', 'CastellaBot']:
        print(f"Commencing evolutionary stage against: {opponent}")
        wins_in_window = 0

        for i in range(1, (epochs_per_stage * window_size) + 1):
            won = play_match(agent, opponent)
            if won: wins_in_window += 1

            if i % window_size == 0:
                win_rate = (wins_in_window * 100.0) / window_size
                wins_in_window = 0
                epoch_counter += 1

                log_data.append({
                    'epoch': epoch_counter,
                    'opponent': opponent,
                    'win_rate': win_rate
                })

    df = pd.DataFrame(log_data)
    df.to_csv("training_metrics.csv", index=False)
    print("\nTraining Metrics saved successfully to training_metrics.csv!")

    pivot_df = df.pivot(index='epoch', columns='opponent', values='win_rate')
    pivot_df.plot(marker='o', linewidth=2, figsize=(10, 6))

    plt.title("AnmitsuBot Accelerated Training Performance")
    plt.xlabel("Training Epoch Data Frames")
    plt.ylabel("Win Rate Percentage (%)")
    plt.grid(True, linestyle='--', alpha=0.6)
    plt.ylim(-5, 105)

    print("\nDisplaying Performance Chart. Final weight profiles:")
    print("Opening Layer Vector:\n", np.round(agent.weights['OPENING'], 4))
    print("Midgame Layer Vector:\n", np.round(agent.weights['MIDGAME'], 4))
    print("Endgame Layer Vector:\n", np.round(agent.weights['ENDGAME'], 4))

    plt.savefig("training_performance.png", dpi=300, bbox_inches='tight')
    print("\nPerformance plot successfully exported as 'training_performance.png'!")