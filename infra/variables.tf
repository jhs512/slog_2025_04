variable "region" {
  description = "region"
  default     = "ap-northeast-2"
}

variable "prefix" {
  description = "Prefix for all resources"
  default     = "dev"
}

variable "app_1_domain" {
  description = "app_1 domain"
  default     = "api.slog.gg" // API 서버 도메인
}

variable "app_1_db_name" {
  description = "app_1 db_name"
  default     = "slog_prod" // mysql 데이터베이스 이름
}