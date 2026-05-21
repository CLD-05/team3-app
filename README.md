# 📝 Foldy

> 10~20대 학습자를 위한 폴더형 메모 서비스  
> EKS 위에 GitOps 방식으로 배포

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green)
![EKS](https://img.shields.io/badge/AWS-EKS-FF9900)

---

## 📌 서비스 소개

Foldy는 공부 내용을 폴더와 태그로 정리할 수 있는 메모 서비스입니다.  
학습자가 자신만의 노트를 체계적으로 관리할 수 있도록 돕습니다.

---

## 🛠 기술 스택

| 영역 | 기술 |
|------|------|
| Backend | Spring Boot 3.5, JPA, Spring Security |
| Frontend | Thymeleaf, Bootstrap |
| DB | MySQL 8.4 (RDS) |
| Storage | AWS S3 |
| Container | Docker, Amazon ECR |
| Orchestration | Amazon EKS |
| IaC | Terraform |
| CI | GitHub Actions (OIDC) |
| CD | ArgoCD (GitOps) |
| Monitoring | kube-prometheus-stack, Grafana |

---

## 🌿 브랜치 전략

| 브랜치 | 설명 |
|--------|------|
| `main` | 최종 배포 브랜치 |
| `develop` | 통합 개발 브랜치 |
| `feature/*` | 기능 개발 |
| `fix/*` | 버그 수정 |
| `chore/*` | 설정/빌드 변경 |
| `ci/*` | CI/CD 변경 |
| `infra/*` | 인프라 변경 |

### 브랜치 네이밍 규칙
형식: 타입/이름_날짜/기능설명

### 예시

```bash
feature/HKD_20260522/login-api
feature/HKD_20260522/memo-crud
fix/HKD_20260522/jwt-expire-error
chore/HKD_20260522/dockerfile-update
infra/HKD_20260522/vpc-module
```

---

## 📝 커밋 컨벤션
형식: <타입>: <한 줄 요약>

| 타입 | 설명 |
|------|------|
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 리팩토링 |
| `chore` | 빌드/설정 변경 |
| `ci` | CI/CD 변경 |
| `infra` | 인프라 변경 |
| `docs` | 문서 수정 |

### ✅ 좋은 예

```bash
feat: 메모 생성 API 구현
fix: JWT 만료 처리 오류 수정
chore: Dockerfile JDK 17 변경
ci: GitHub Actions OIDC role-to-assume 적용
infra: EKS 노드그룹 인스턴스 타입 변경
```

### ❌ 나쁜 예

```bash
git commit -m "수정"
git commit -m "작업중"
git commit -m "aaaa"
```


---

## 🔀 PR 규칙

1. `feature/*` → `develop` 방향으로만 PR 생성
2. PR 제목 형식: `[feat] 메모 생성 API 구현`
3. PR 본문 템플릿 반드시 작성
4. 인프라 변경 PR은 `terraform plan` 결과 첨부 필수
5. 팀원 1인 이상 리뷰 후 approve → merge
6. **approve 없이 merge 금지**
