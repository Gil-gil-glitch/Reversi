import numpy as np
import pandas as pd

class AnmitsuBotPython:
    def __init__(self):
        # 10 features for Opening, Midgame, Endgame
        self.weights = {
            'opening': np.random.uniform(-0.1, 0.1, 10),
            'midgame': np.random.uniform(-0.1, 0.1, 10),
            'endgame': np.random.uniform(-0.1, 0.1, 10)
        }
        self.alpha = 0.01

    def train_on_history(self, history, final_score_diff):
        # Target evaluation scale matches your Java target precisely
        target_value = final_score_diff / 64.0 
        
        # Backward Temporal Difference updates
        for features, phase in reversed(history):
            w = self.weights[phase]
            predicted = np.dot(features, w)
            td_error = target_value - predicted
            
            # Vectorized execution step: no nested loops!
            self.weights[phase] += self.alpha * td_error * features
            target_value = predicted