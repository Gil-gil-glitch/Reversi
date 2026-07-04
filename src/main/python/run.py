import pandas as pd
import matplotlib.pyplot as plt

# Track performance history
log_data = []

# Inside training loops
log_data.append({
    'epoch': current_epoch,
    'opponent': 'TaiyakiBot',
    'win_rate': current_win_rate,
    'mean_weight_val': agent.weights['midgame'].mean()
})

# Convert to Pandas DataFrame for instant data science manipulation
df = pd.DataFrame(log_data)
df.to_csv("training_metrics.csv", index=False)

# Render crisp multi-line training plots
df.pivot(index='epoch', columns='opponent', values='win_rate').plot()
plt.ylabel("Win Rate (%)")
plt.show()