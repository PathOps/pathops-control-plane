package io.pathops.controlplane.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "membership_provisionings",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_membership_tool", columnNames = {"membership_id", "tool"})
    }
)
@Getter
@Setter
@NoArgsConstructor
public class MembershipProvisioning extends AbstractProvisioning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_id", nullable = false)
    private Membership membership;

    @Enumerated(EnumType.STRING)
    @Column(name = "tool", nullable = false)
    private MembershipProvisioningTool tool;
}