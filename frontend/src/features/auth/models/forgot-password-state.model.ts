export interface ForgotPasswordState {
  readonly submitted: boolean;
  readonly cooldownSeconds: number; // 60 → 0 countdown
  readonly canSubmit: boolean; // computed: !submitted || cooldownSeconds === 0
}
