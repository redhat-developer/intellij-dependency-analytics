# hadolint ignore=DL3006
FROM rust:1 AS builder

WORKDIR /app

COPY . ./

RUN cargo build --locked --release

# hadolint ignore=DL3006
FROM gcr.io/distroless/cc-debian12 AS runner

ENV RUST_LOG="hello_world=debug,info"

COPY --from=builder /app/target/release/rust-dockerfiled-hello-world /

EXPOSE 3000

ENTRYPOINT ["./rust-dockerfiled-hello-world"]
CMD ["./rust-dockerfiled-hello-world"]
