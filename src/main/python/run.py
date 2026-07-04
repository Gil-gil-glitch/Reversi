import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import reversi_engine as re
from train_agent import AnmitsuBotPython

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

                # Epsilon-Greedy exploration (10%)
                if np.random.rand() < 0.1:
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

    # Execute reinforcement pass
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

    # --- Pandas and Matplotlib Telemetry Pass ---
    df = pd.DataFrame(log_data)
    df.to_csv("training_metrics.csv", index=False)
    print("\nTraining Metrics saved successfully to training_metrics.csv!")

    # Pivot matrix configurations to draw clean multi-line progressions
    pivot_df = df.pivot(index='epoch', columns='opponent', values='win_rate')
    pivot_df.plot(marker='o', linewidth=2, figsize=(10, 6))

    plt.title("AnmitsuBot Accelerated Training Performance")
    plt.xlabel("Training Epoch Data Frames")
    plt.ylabel("Win Rate Percentage (%)")
    plt.grid(True, linestyle='--', alpha=0.6)
    plt.ylim(-5, 105)

    print("\nDisplaying Performance Chart. Final weight profiles:")
    print("Opening Layer Vector:", np.round(agent.weights['OPENING'], 4))
    print("Midgame Layer Vector:", np.round(agent.weights['MIDGAME'], 4))
    print("Endgame Layer Vector:", np.round(agent.weights['ENDGAME'], 4))

    plt.savefig("training_performance.png", dpi=300, bbox_inches='tight')
    print("Performance plot successfully exported as 'training_performance.png'!")